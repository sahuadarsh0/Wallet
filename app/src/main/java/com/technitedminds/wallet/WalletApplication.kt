package com.technitedminds.wallet

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for CardVault wallet app.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class WalletApplication : Application() {
    companion object {
        @JvmStatic
        lateinit var instance: WalletApplication
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        // Application initialization code will go here
    }
    
    fun getContext() = applicationContext
}