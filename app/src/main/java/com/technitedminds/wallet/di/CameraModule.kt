package com.technitedminds.wallet.di

import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.technitedminds.wallet.data.ocr.CardTextParser
import com.technitedminds.wallet.data.ocr.MLKitTextRecognizer
import com.technitedminds.wallet.data.service.CardImageGeneratorImpl
import com.technitedminds.wallet.data.service.OCRServiceImpl
import com.technitedminds.wallet.data.service.StorageServiceImpl
import com.technitedminds.wallet.domain.service.CardImageGenerator
import com.technitedminds.wallet.domain.service.OCRService
import com.technitedminds.wallet.domain.service.StorageService
import com.technitedminds.wallet.presentation.components.camera.CameraManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing camera and OCR related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object CameraModule {
    
    @Provides
    @Singleton
    fun provideTextRecognizer(): TextRecognizer {
        return TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }
    
    @Provides
    @Singleton
    fun provideCardTextParser(): CardTextParser {
        return CardTextParser()
    }
    
    @Provides
    @Singleton
    fun provideMLKitTextRecognizer(
        cardTextParser: CardTextParser
    ): MLKitTextRecognizer {
        return MLKitTextRecognizer(cardTextParser)
    }
    
    @Provides
    @Singleton
    fun provideCameraManager(): CameraManager {
        return CameraManager()
    }
}

/**
 * Hilt module for binding service interfaces to implementations.
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