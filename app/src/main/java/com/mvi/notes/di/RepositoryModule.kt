package com.mvi.notes.di

import com.mvi.notes.RepositoryQualifiers
import com.mvi.notes.data.source.encrypted.keystorage.NoteDataSource
import com.mvi.notes.domain.repository.NoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Named(RepositoryQualifiers.ROOM)
    @Singleton
    abstract fun bindRoomRepository(
        impl: com.mvi.notes.data.source.room.NoteDataSource
    ): NoteRepository

    @Binds
    @Named(RepositoryQualifiers.SHARED)
    @Singleton
    abstract fun bindSharedPrefRepository(
        impl: com.mvi.notes.data.source.sharedpref.NoteDataSource
    ): NoteRepository

    @Binds
    @Named(RepositoryQualifiers.ENCRYPTED_SHARED)
    @Singleton
    abstract fun bindEncryptedSharedPrefRepository(
        impl: com.mvi.notes.data.source.encrypted.EncryptedNoteDataSource
    ): NoteRepository

    @Binds
    @Named(RepositoryQualifiers.FILE)
    @Singleton
    abstract fun bindFileRepository(
        impl: com.mvi.notes.data.source.file.NoteDataSource
    ): NoteRepository

    @Binds
    @Named(RepositoryQualifiers.SCOPED)
    @Singleton
    abstract fun bindScopedStorageRepository(
        impl: com.mvi.notes.data.source.scopedstorage.NoteDataSource
    ): NoteRepository


    @Binds
    @Named(RepositoryQualifiers.KEY_STORE)
    @Singleton
    abstract fun bindKeyStorageRepository(
        impl: NoteDataSource
    ): NoteRepository


}
