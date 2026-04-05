package com.technitedminds.wallet.data.nfc

import android.nfc.tech.IsoDep
import com.technitedminds.wallet.domain.model.NfcCardData
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads EMV payment card data over NFC (IsoDep / ISO 14443-4).
 *
 * Sequence: SELECT PPSE -> SELECT AID -> GET PROCESSING OPTIONS -> READ RECORD.
 * Extracts PAN, expiry date, and cardholder name from standard EMV tags.
 */
@Singleton
class EmvCardReader @Inject constructor(
    private val tlvParser: TlvParser,
) {

    fun readCard(isoDep: IsoDep): NfcCardData {
        isoDep.timeout = TIMEOUT_MS

        // Step 1: SELECT PPSE to discover payment apps
        val ppseResponse = isoDep.transceive(SELECT_PPSE)
        checkSw(ppseResponse)

        // Step 2: Find an AID from the PPSE response, or try known AIDs
        val aid = findAid(ppseResponse)
            ?: tryKnownAids(isoDep)
            ?: throw NfcReadException("This doesn't appear to be a payment card, or it doesn't support contactless reading.")
        val scheme = identifyScheme(aid)

        // Step 3: SELECT the payment application by AID
        val selectAidCmd = buildSelectCommand(aid)
        val selectResponse = isoDep.transceive(selectAidCmd)
        checkSw(selectResponse)

        val selectTags = tlvParser.parse(stripSw(selectResponse))
        val pdol = selectTags[TAG_PDOL]

        // Step 4: GET PROCESSING OPTIONS with proper PDOL values
        val gpoCmd = buildGpoCommand(pdol)
        val gpoResponse = isoDep.transceive(gpoCmd)
        checkSw(gpoResponse)

        val gpoData = stripSw(gpoResponse)
        val gpoTags = parseGpoResponse(gpoData)

        var cardNumber = ""
        var expiryDate = ""
        var cardholderName = ""

        extractFromTags(gpoTags).let { (pan, exp, name) ->
            cardNumber = pan
            expiryDate = exp
            cardholderName = name
        }

        // Step 5: READ RECORD using AFL from GPO response
        val afl = gpoTags[TAG_AFL]
        if (afl != null) {
            val recordTags = readRecords(isoDep, afl)
            extractFromTags(recordTags).let { (pan, exp, name) ->
                if (cardNumber.isBlank()) cardNumber = pan
                if (expiryDate.isBlank()) expiryDate = exp
                if (cardholderName.isBlank()) cardholderName = name
            }
        }

        if (cardNumber.isBlank()) {
            throw NfcReadException("Could not read the card details. Hold the card flat and steady, then try again.")
        }

        return NfcCardData(
            cardNumber = formatPan(cardNumber),
            expiryDate = formatExpiry(expiryDate),
            cardholderName = formatName(cardholderName),
            aid = aid.toHexString(),
            cardScheme = scheme,
        )
    }

    /**
     * GPO can return data in two formats:
     * - Format 1 (tag 80): raw concatenation of AIP (2 bytes) + AFL
     * - Format 2 (tag 77): standard BER-TLV with individual tags
     */
    private fun parseGpoResponse(data: ByteArray): Map<String, ByteArray> {
        val tags = tlvParser.parse(data)

        // Format 2: tag 77 was parsed and contains sub-tags like 94 (AFL)
        if (tags.containsKey(TAG_AFL)) return tags

        // Format 1: tag 80 contains AIP (2 bytes) + AFL (rest)
        tags["80"]?.let { raw ->
            val result = tags.toMutableMap()
            if (raw.size > 2) {
                result[TAG_AFL] = raw.copyOfRange(2, raw.size)
            }
            return result
        }

        return tags
    }

    // ---- APDU builders ----

    private fun buildSelectCommand(aid: ByteArray): ByteArray {
        return byteArrayOf(
            0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(),
            aid.size.toByte(),
        ) + aid + byteArrayOf(0x00.toByte())
    }

    private fun buildGpoCommand(pdol: ByteArray?): ByteArray {
        if (pdol == null || pdol.isEmpty()) {
            // No PDOL requested — send empty command data
            return "80A8000002830000".hexToByteArray()
        }

        // Build PDOL response with proper default values for each requested tag
        val pdolData = buildPdolResponse(pdol)
        val dataField = byteArrayOf(0x83.toByte(), pdolData.size.toByte()) + pdolData

        return byteArrayOf(
            0x80.toByte(), 0xA8.toByte(), 0x00.toByte(), 0x00.toByte(),
            dataField.size.toByte(),
        ) + dataField + byteArrayOf(0x00.toByte())
    }

    /**
     * Build the PDOL response by providing sensible default values for each
     * tag the card requests. The PDOL is a sequence of (tag, length) pairs
     * telling us what terminal data the card needs for GPO.
     */
    private fun buildPdolResponse(pdol: ByteArray): ByteArray {
        val result = mutableListOf<Byte>()
        var i = 0

        while (i < pdol.size) {
            // Read tag
            val tagStart = i
            val firstByte = pdol[i].toInt() and 0xFF
            i++
            if (firstByte and 0x1F == 0x1F) {
                while (i < pdol.size && pdol[i].toInt() and 0x80 != 0) i++
                if (i < pdol.size) i++
            }
            val tag = pdol.sliceArray(tagStart until i).toHexString().uppercase()

            // Read length
            if (i >= pdol.size) break
            val len = pdol[i].toInt() and 0xFF
            i++

            // Provide appropriate default value for this tag
            val value = getDefaultPdolValue(tag, len)
            result.addAll(value.toList())
        }

        return result.toByteArray()
    }

    /**
     * Returns a sensible default value for an EMV terminal tag.
     * These values simulate a contactless-capable terminal performing a read.
     */
    private fun getDefaultPdolValue(tag: String, length: Int): ByteArray {
        val value = when (tag) {
            // Terminal Transaction Qualifiers — contactless MSD + qVSDC supported
            "9F66" -> "B6000000".hexToByteArray()
            // Amount Authorized — zero (we're just reading, not transacting)
            "9F02" -> "000000000000".hexToByteArray()
            // Amount Other — zero
            "9F03" -> "000000000000".hexToByteArray()
            // Terminal Country Code — US (0840)
            "9F1A" -> "0840".hexToByteArray()
            // Transaction Currency Code — USD (0840)
            "5F2A" -> "0840".hexToByteArray()
            // Transaction Date — today-ish (doesn't need to be exact for reading)
            "9A" -> "260101".hexToByteArray()
            // Transaction Type — purchase
            "9C" -> "00".hexToByteArray()
            // Unpredictable Number — random 4 bytes
            "9F37" -> ByteArray(4).also { java.security.SecureRandom().nextBytes(it) }
            // Terminal Type — attended online-capable
            "9F35" -> "22".hexToByteArray()
            // Terminal Capabilities
            "9F33" -> "E0F0C8".hexToByteArray()
            // Additional Terminal Capabilities
            "9F40" -> "F000F0A001".hexToByteArray()
            // Transaction Sequence Counter
            "9F41" -> "00000001".hexToByteArray()
            // Anything else — fill with zeros
            else -> ByteArray(length)
        }

        // Pad or truncate to the exact length the card requested
        return when {
            value.size == length -> value
            value.size > length -> value.copyOfRange(0, length)
            else -> value + ByteArray(length - value.size)
        }
    }

    private fun buildReadRecordCommand(sfi: Int, record: Int): ByteArray {
        return byteArrayOf(
            0x00.toByte(), 0xB2.toByte(),
            record.toByte(),
            ((sfi shl 3) or 0x04).toByte(),
            0x00.toByte(),
        )
    }

    // ---- Record reading ----

    private fun readRecords(isoDep: IsoDep, afl: ByteArray): Map<String, ByteArray> {
        val allTags = mutableMapOf<String, ByteArray>()
        var i = 0
        while (i + 3 < afl.size) {
            val sfi = (afl[i].toInt() and 0xFF) shr 3
            val firstRecord = afl[i + 1].toInt() and 0xFF
            val lastRecord = afl[i + 2].toInt() and 0xFF
            i += 4

            for (record in firstRecord..lastRecord) {
                try {
                    val response = isoDep.transceive(buildReadRecordCommand(sfi, record))
                    if (isSuccess(response)) {
                        allTags.putAll(tlvParser.parse(stripSw(response)))
                    }
                } catch (_: Exception) {
                    // Tag lost or record not available — continue with other records
                }
            }
        }
        return allTags
    }

    // ---- Data extraction ----

    private fun extractFromTags(tags: Map<String, ByteArray>): Triple<String, String, String> {
        var pan = ""
        var expiry = ""
        var name = ""

        // Tag 5A — Application PAN
        tags[TAG_PAN]?.let { pan = it.toHexString().trimEnd('F') }

        // Tag 5F24 — Application Expiration Date (YYMMDD)
        tags[TAG_EXPIRY]?.let { expiry = it.toHexString() }

        // Tag 5F20 — Cardholder Name (ASCII)
        tags[TAG_CARDHOLDER_NAME]?.let {
            val parsed = String(it, Charsets.US_ASCII).trim()
            if (parsed.isNotBlank() && parsed != "/") name = parsed
        }

        // Fallback: tag 9F0B — Cardholder Name Extended (some issuers use this instead)
        if (name.isBlank()) {
            tags[TAG_CARDHOLDER_NAME_EXT]?.let {
                val parsed = String(it, Charsets.US_ASCII).trim()
                if (parsed.isNotBlank() && parsed != "/") name = parsed
            }
        }

        // Fallback: Track 2 Equivalent (tag 57) contains PAN and expiry
        if (pan.isBlank()) {
            tags[TAG_TRACK2]?.let { track2 ->
                parseTrack2(track2.toHexString()).let { (t2Pan, t2Exp) ->
                    if (pan.isBlank()) pan = t2Pan
                    if (expiry.isBlank()) expiry = t2Exp
                }
            }
        }

        // Second fallback: tag 9F6B (Track 2 Data for contactless)
        if (pan.isBlank()) {
            tags[TAG_TRACK2_CL]?.let { track2 ->
                parseTrack2(track2.toHexString()).let { (t2Pan, t2Exp) ->
                    if (pan.isBlank()) pan = t2Pan
                    if (expiry.isBlank()) expiry = t2Exp
                }
            }
        }

        return Triple(pan, expiry, name)
    }

    private fun parseTrack2(hex: String): Pair<String, String> {
        val cleaned = hex.uppercase().trimEnd('F')
        val separatorIdx = cleaned.indexOfFirst { it == 'D' || it == '=' }
        if (separatorIdx < 0) return Pair("", "")
        val pan = cleaned.substring(0, separatorIdx)
        val rest = cleaned.substring(separatorIdx + 1)
        val expiry = if (rest.length >= 4) rest.substring(0, 4) else ""
        return Pair(pan, expiry)
    }

    // ---- AID discovery ----

    private fun findAid(ppseResponse: ByteArray): ByteArray? {
        val tags = tlvParser.parse(stripSw(ppseResponse))

        // Tag 4F — Application Identifier (AID), may be nested in 61/A5 templates
        tags[TAG_AID_DF]?.let { return it }

        return null
    }

    /**
     * If PPSE didn't yield an AID, try selecting well-known payment AIDs directly.
     * Returns the first AID that the card accepts.
     */
    private fun tryKnownAids(isoDep: IsoDep): ByteArray? {
        for (aid in KNOWN_AIDS) {
            try {
                val aidBytes = aid.hexToByteArray()
                val response = isoDep.transceive(buildSelectCommand(aidBytes))
                if (isSuccess(response)) {
                    return aidBytes
                }
            } catch (_: Exception) {
                // Try next AID
            }
        }
        return null
    }

    private fun identifyScheme(aid: ByteArray): String {
        val hex = aid.toHexString().uppercase()
        return when {
            hex.startsWith("A000000003") -> "Visa"
            hex.startsWith("A000000004") -> "Mastercard"
            hex.startsWith("A000000025") -> "American Express"
            hex.startsWith("A000000065") -> "JCB"
            hex.startsWith("A000000001") -> "Discover"
            hex.startsWith("A000000152") -> "Discover"
            hex.startsWith("A000000333") -> "UnionPay"
            else -> "Unknown"
        }
    }

    // ---- Formatting helpers ----

    private fun formatPan(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return digits.chunked(4).joinToString(" ")
    }

    private fun formatExpiry(raw: String): String {
        // EMV stores YYMM or YYMMDD
        val digits = raw.filter { it.isDigit() }
        if (digits.length < 4) return raw
        val yy = digits.substring(0, 2)
        val mm = digits.substring(2, 4)
        return "$mm/$yy"
    }

    private fun formatName(raw: String): String {
        // EMV stores name as LAST/FIRST or may use special separators
        return raw
            .replace("/", " ")
            .replace("\\", " ")
            .trim()
            .split("\\s+".toRegex())
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }
    }

    // ---- Response validation ----

    private fun checkSw(response: ByteArray) {
        if (!isSuccess(response)) {
            if (response.size >= 2) {
                val sw1 = response[response.size - 2].toInt() and 0xFF
                val sw2 = response[response.size - 1].toInt() and 0xFF
                throw NfcReadException(friendlySwMessage(sw1, sw2))
            }
            throw NfcReadException("Could not communicate with the card. Try repositioning it.")
        }
    }

    private fun friendlySwMessage(sw1: Int, sw2: Int): String = when {
        sw1 == 0x69 && sw2 == 0x85 -> "This card has restricted contactless access. Try using the camera to capture it instead."
        sw1 == 0x69 && sw2 == 0x84 -> "This card does not allow contactless reading."
        sw1 == 0x69 && sw2 == 0x86 -> "This card requires a different reading method. Try the camera instead."
        sw1 == 0x69 && sw2 == 0x99 -> "This card requires PIN verification and cannot be read contactlessly."
        sw1 == 0x6A && sw2 == 0x82 -> "No payment application found on this card."
        sw1 == 0x6A && sw2 == 0x81 -> "This card type is not supported for contactless reading."
        sw1 == 0x6A && sw2 == 0x86 -> "Could not read card data. Try repositioning the card."
        sw1 == 0x67 && sw2 == 0x00 -> "Could not communicate with the card. Try again."
        sw1 == 0x6E && sw2 == 0x00 -> "This card type is not supported."
        sw1 == 0x6D && sw2 == 0x00 -> "This card does not support contactless reading."
        sw1 == 0x63 -> "Card verification failed. Please try again."
        sw1 == 0x62 || sw1 == 0x64 -> "Could not read card data. Hold the card steady and try again."
        else -> "Could not read this card. Hold it flat against the back of your phone and try again."
    }

    private fun isSuccess(response: ByteArray): Boolean {
        if (response.size < 2) return false
        val sw1 = response[response.size - 2].toInt() and 0xFF
        val sw2 = response[response.size - 1].toInt() and 0xFF
        // 9000 = success, 61XX = success with more data available
        return (sw1 == 0x90 && sw2 == 0x00) || sw1 == 0x61
    }

    private fun stripSw(response: ByteArray): ByteArray =
        if (response.size > 2) response.copyOfRange(0, response.size - 2) else ByteArray(0)

    companion object {
        private const val TIMEOUT_MS = 5000

        private val SELECT_PPSE = "00A404000E325041592E5359532E444446303100".hexToByteArray()

        // EMV tag identifiers
        private const val TAG_PAN = "5A"
        private const val TAG_EXPIRY = "5F24"
        private const val TAG_CARDHOLDER_NAME = "5F20"
        private const val TAG_TRACK2 = "57"
        private const val TAG_TRACK2_CL = "9F6B"
        private const val TAG_AFL = "94"
        private const val TAG_PDOL = "9F38"
        private const val TAG_AID_DF = "4F"
        private const val TAG_CARDHOLDER_NAME_EXT = "9F0B"
        private const val TAG_APP_LABEL = "50"

        // Well-known payment application AIDs for fallback selection
        private val KNOWN_AIDS = listOf(
            "A0000000031010",   // Visa Credit/Debit
            "A0000000032010",   // Visa Electron
            "A0000000041010",   // Mastercard Credit/Debit
            "A0000000043060",   // Mastercard Maestro
            "A000000025010104", // American Express
            "A0000000651010",   // JCB
            "A0000001523010",   // Discover
            "A0000003330101",   // UnionPay Debit
            "A0000000040000",   // Mastercard (legacy)
        )
    }
}

class NfcReadException(message: String) : Exception(message)
