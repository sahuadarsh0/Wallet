package com.technitedminds.wallet.di

import android.content.Context
import com.technitedminds.wallet.data.local.preferences.SimplePreferencesManager
import com.technitedminds.wallet.data.local.security.AppLockRepository
import com.technitedminds.wallet.data.local.security.PinHasher
import com.technitedminds.wallet.data.local.security.RecoveryCodeManager
import com.technitedminds.wallet.data.local.security.TinkEncryptionManager
import com.technitedminds.wallet.presentation.screens.security.BiometricAuthManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing security-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideTinkEncryptionManager(
        @ApplicationContext context: Context,
    ): TinkEncryptionManager = TinkEncryptionManager(context)

    @Provides
    @Singleton
    fun providePinHasher(): PinHasher = PinHasher()

    @Provides
    @Singleton
    fun provideRecoveryCodeManager(pinHasher: PinHasher): RecoveryCodeManager =
        RecoveryCodeManager(pinHasher)

    @Provides
    @Singleton
    fun provideAppLockRepository(
        @ApplicationContext context: Context,
        preferencesManager: SimplePreferencesManager,
        pinHasher: PinHasher,
        recoveryCodeManager: RecoveryCodeManager,
    ): AppLockRepository = AppLockRepository(context, preferencesManager, pinHasher, recoveryCodeManager)

    @Provides
    @Singleton
    fun provideBiometricAuthManager(
        @ApplicationContext context: Context,
    ): BiometricAuthManager = BiometricAuthManager(context)
}
