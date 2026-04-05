package com.technitedminds.wallet.presentation.screens.addcard

import android.app.Activity
import android.nfc.NfcAdapter
import com.technitedminds.wallet.data.nfc.NfcReaderCallback
import com.technitedminds.wallet.domain.model.NfcCardData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * State sealed interface representing all possible NFC reading states.
 */
sealed interface NfcReadState {
    data object Idle : NfcReadState
    data object Scanning : NfcReadState
    data class Success(val data: NfcCardData) : NfcReadState
    data class Error(val message: String) : NfcReadState
    data object NotAvailable : NfcReadState
}

/**
 * Manages NFC reader mode lifecycle for EMV contactless card reading.
 *
 * Call [startReading] with an Activity to enable NFC reader mode and
 * [stopReading] to disable it. Observe [state] for scanning progress.
 */
@Singleton
class NfcCardReaderManager @Inject constructor(
    private val nfcReaderCallback: NfcReaderCallback,
) {
    private val _state = MutableStateFlow<NfcReadState>(NfcReadState.Idle)
    val state: StateFlow<NfcReadState> = _state.asStateFlow()

    private var nfcAdapter: NfcAdapter? = null

    /**
     * Check if NFC hardware is present and enabled on the device.
     */
    fun isNfcAvailable(activity: Activity): Boolean {
        val adapter = NfcAdapter.getDefaultAdapter(activity)
        return adapter != null
    }

    fun isNfcEnabled(activity: Activity): Boolean {
        val adapter = NfcAdapter.getDefaultAdapter(activity)
        return adapter?.isEnabled == true
    }

    /**
     * Enable NFC reader mode and begin scanning for contactless cards.
     */
    fun startReading(activity: Activity) {
        val adapter = NfcAdapter.getDefaultAdapter(activity)
        if (adapter == null) {
            _state.value = NfcReadState.NotAvailable
            return
        }
        if (!adapter.isEnabled) {
            _state.value = NfcReadState.Error("NFC is disabled. Please enable it in your device settings.")
            return
        }

        nfcAdapter = adapter
        _state.value = NfcReadState.Scanning

        nfcReaderCallback.onResult = { data ->
            _state.value = NfcReadState.Success(data)
            disableReaderMode(activity)
        }
        nfcReaderCallback.onError = { message ->
            _state.value = NfcReadState.Error(message)
        }

        adapter.enableReaderMode(
            activity,
            nfcReaderCallback,
            NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null,
        )
    }

    /**
     * Stop NFC reader mode and reset state to Idle.
     */
    fun stopReading(activity: Activity) {
        disableReaderMode(activity)
        _state.value = NfcReadState.Idle
    }

    /**
     * Reset state back to Idle without disabling reader mode (for retry flows).
     */
    fun resetState() {
        _state.value = NfcReadState.Idle
    }

    private fun disableReaderMode(activity: Activity) {
        try {
            nfcAdapter?.disableReaderMode(activity)
        } catch (_: Exception) {
            // Activity may have been destroyed
        }
        nfcReaderCallback.onResult = null
        nfcReaderCallback.onError = null
    }
}
