package com.technitedminds.wallet.data.nfc

import android.nfc.Tag
import android.nfc.tech.IsoDep
import com.technitedminds.wallet.domain.model.NfcCardData
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NFC reader callback that bridges [android.nfc.NfcAdapter.ReaderCallback]
 * with the [EmvCardReader]. When a tag is discovered, it attempts to read
 * EMV data and delivers the result via [onResult] / [onError] lambdas.
 */
@Singleton
class NfcReaderCallback @Inject constructor(
    private val emvCardReader: EmvCardReader,
) : android.nfc.NfcAdapter.ReaderCallback {

    var onResult: ((NfcCardData) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    override fun onTagDiscovered(tag: Tag?) {
        if (tag == null) {
            onError?.invoke("No card detected. Make sure your card is nearby and try again.")
            return
        }

        val isoDep = IsoDep.get(tag)
        if (isoDep == null) {
            onError?.invoke("This card doesn't support contactless reading. Try using the camera instead.")
            return
        }

        try {
            isoDep.connect()
            val data = emvCardReader.readCard(isoDep)
            onResult?.invoke(data)
        } catch (e: NfcReadException) {
            onError?.invoke(e.message ?: "Could not read card. Please try again.")
        } catch (e: android.nfc.TagLostException) {
            onError?.invoke("Card moved away too quickly. Hold it flat and steady against your phone.")
        } catch (e: java.io.IOException) {
            onError?.invoke("Lost connection to the card. Place it back and try again.")
        } catch (e: Exception) {
            onError?.invoke("Something went wrong. Please try again.")
        } finally {
            try {
                isoDep.close()
            } catch (_: Exception) {
                // Ignore close errors
            }
        }
    }
}
