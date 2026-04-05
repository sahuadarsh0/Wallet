package com.technitedminds.wallet.di

import com.technitedminds.wallet.data.service.CardImageGeneratorImpl
import com.technitedminds.wallet.data.service.OCRServiceImpl
import com.technitedminds.wallet.data.service.StorageServiceImpl
import com.technitedminds.wallet.domain.service.CardImageGenerator
import com.technitedminds.wallet.domain.service.OCRService
import com.technitedminds.wallet.domain.service.StorageService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for binding service interfaces to implementations.
 *
 * Concrete classes (CardTextParser, MLKitTextRecognizer, CameraManager) are
 * self-provided via @Inject constructor + @Singleton on their class declarations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceBindingsModule {

    @Binds
    @Singleton
    abstract fun bindOCRService(impl: OCRServiceImpl): OCRService

    @Binds
    @Singleton
    abstract fun bindStorageService(impl: StorageServiceImpl): StorageService

    @Binds
    @Singleton
    abstract fun bindCardImageGenerator(impl: CardImageGeneratorImpl): CardImageGenerator
}
