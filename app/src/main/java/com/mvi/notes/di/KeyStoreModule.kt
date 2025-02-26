package com.mvi.notes.di

import com.mvi.notes.data.source.encrypted.keystorage.KeyStoreHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object KeyStoreModule {

    @Provides
    @Singleton
    fun provideKeyStoreHelper(): KeyStoreHelper {
        val keyStoreHelper = KeyStoreHelper()
        keyStoreHelper.generateKey()
        return keyStoreHelper
    }
}
