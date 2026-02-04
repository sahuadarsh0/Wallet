package com.technitedminds.wallet.di

import android.content.Context
import com.technitedminds.wallet.data.local.database.WalletDatabase
import com.technitedminds.wallet.data.local.database.dao.CardDao
import com.technitedminds.wallet.data.local.database.dao.CategoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideWalletDatabase(
        @ApplicationContext context: Context
    ): WalletDatabase {
        return WalletDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideCardDao(database: WalletDatabase): CardDao {
        return database.cardDao()
    }
    
    @Provides
    fun provideCategoryDao(database: WalletDatabase): CategoryDao {
        return database.categoryDao()
    }
}