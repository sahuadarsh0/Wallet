package com.technitedminds.wallet.di

import com.technitedminds.wallet.data.nfc.EmvCardReader
import com.technitedminds.wallet.data.nfc.NfcReaderCallback
import com.technitedminds.wallet.data.nfc.TlvParser
import com.technitedminds.wallet.presentation.screens.addcard.NfcCardReaderManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NfcModule {

    @Provides
    @Singleton
    fun provideTlvParser(): TlvParser = TlvParser()

    @Provides
    @Singleton
    fun provideEmvCardReader(tlvParser: TlvParser): EmvCardReader =
        EmvCardReader(tlvParser)

    @Provides
    @Singleton
    fun provideNfcReaderCallback(emvCardReader: EmvCardReader): NfcReaderCallback =
        NfcReaderCallback(emvCardReader)

    @Provides
    @Singleton
    fun provideNfcCardReaderManager(nfcReaderCallback: NfcReaderCallback): NfcCardReaderManager =
        NfcCardReaderManager(nfcReaderCallback)
}
