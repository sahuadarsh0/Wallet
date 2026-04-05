package com.technitedminds.wallet

import android.app.Application
import com.technitedminds.wallet.data.local.security.TinkEncryptionManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class for CardVault wallet app.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class WalletApplication : Application() {

    @Inject lateinit var tinkEncryptionManager: TinkEncryptionManager

    companion object {
        @JvmStatic
        lateinit var instance: WalletApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun getContext() = applicationContext
}
