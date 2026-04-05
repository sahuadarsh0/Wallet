package com.technitedminds.wallet.data.nfc

import javax.inject.Inject
import javax.inject.Singleton

/**
 * BER-TLV parser for EMV contactless card APDU responses.
 *
 * Parses tag-length-value structures from byte arrays and returns a flat map
 * of hex-encoded tag identifiers to their raw byte-array values. Handles
 * single-byte, two-byte, and three-byte tags as well as constructed
 * (container) tags that are recursively expanded.
 */
@Singleton
class TlvParser @Inject constructor() {

    /**
     * Parse a byte array containing BER-TLV data into a tag -> value map.
     * Constructed tags are recursively expanded so leaf values are always
     * reachable by their tag key (e.g. "5A", "5F24").
     */
    fun parse(data: ByteArray): Map<String, ByteArray> {
        val result = mutableMapOf<String, ByteArray>()
        parseTlv(data, 0, data.size, result)
        return result
    }

    private fun parseTlv(
        data: ByteArray,
        startOffset: Int,
        endOffset: Int,
        result: MutableMap<String, ByteArray>,
    ) {
        var offset = startOffset
        while (offset < endOffset) {
            if (data[offset] == 0x00.toByte() || data[offset] == 0xFF.toByte()) {
                offset++
                continue
            }

            val tagStart = offset
            val firstByte = data[offset].toInt() and 0xFF
            offset++

            // Multi-byte tag: if low 5 bits of the first byte are all 1s
            if (firstByte and 0x1F == 0x1F) {
                while (offset < endOffset && data[offset].toInt() and 0x80 != 0) {
                    offset++
                }
                if (offset < endOffset) offset++
            }

            val tag = data.sliceArray(tagStart until offset).toHexString()

            if (offset >= endOffset) break

            // Parse length (BER definite form)
            val (length, lengthBytes) = parseLength(data, offset)
            offset += lengthBytes

            if (length < 0 || offset + length > endOffset) break

            val value = data.sliceArray(offset until offset + length)
            result[tag] = value

            // Recurse into constructed tags (bit 6 of the first byte set)
            if (firstByte and 0x20 != 0 && length > 0) {
                parseTlv(value, 0, value.size, result)
            }

            offset += length
        }
    }

    private fun parseLength(data: ByteArray, offset: Int): Pair<Int, Int> {
        if (offset >= data.size) return Pair(-1, 1)

        val first = data[offset].toInt() and 0xFF
        if (first <= 0x7F) {
            return Pair(first, 1)
        }

        val numBytes = first and 0x7F
        if (numBytes == 0 || offset + 1 + numBytes > data.size) {
            return Pair(-1, 1)
        }

        var length = 0
        for (i in 1..numBytes) {
            length = (length shl 8) or (data[offset + i].toInt() and 0xFF)
        }
        return Pair(length, 1 + numBytes)
    }
}

internal fun ByteArray.toHexString(): String =
    joinToString("") { "%02X".format(it) }

internal fun String.hexToByteArray(): ByteArray {
    val hex = this.replace(" ", "")
    return ByteArray(hex.length / 2) { i ->
        ((hex[i * 2].digitToInt(16) shl 4) or hex[i * 2 + 1].digitToInt(16)).toByte()
    }
}
