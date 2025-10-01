package com.technitedminds.wallet.di

import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.technitedminds.wallet.data.ocr.MLKitTextRecognizer
import com.technitedminds.wallet.presentation.components.camera.CameraManager
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
    fun provideCardTextParser(): com.technitedminds.wallet.data.ocr.CardTextParser {
        return com.technitedminds.wallet.data.ocr.CardTextParser()
    }
    
    @Provides
    @Singleton
    fun provideMLKitTextRecognizer(
        cardTextParser: com.technitedminds.wallet.data.ocr.CardTextParser
    ): MLKitTextRecognizer {
        return MLKitTextRecognizer(cardTextParser)
    }
    
    @Provides
    @Singleton
    fun provideCameraManager(): CameraManager {
        return CameraManager()
    }
}