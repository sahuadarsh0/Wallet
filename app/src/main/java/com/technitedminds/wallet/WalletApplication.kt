package com.technitedminds.wallet

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for CardVault wallet app.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class WalletApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Application initialization code will go here
    }
}