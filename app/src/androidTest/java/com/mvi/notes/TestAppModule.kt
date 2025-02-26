package com.mvi.notes

import android.content.Context
import android.content.SharedPreferences
import com.mvi.notes.domain.repository.NoteRepository
import com.mvi.notes.data.source.room.db.NoteDatabase
import com.mvi.notes.di.AppModule
import dagger.Module
import dagger.Provides
import dagger.hilt.testing.TestInstallIn
import javax.inject.Named
import javax.inject.Singleton
import org.mockito.Mockito

@Module
@TestInstallIn(
    components = [dagger.hilt.components.SingletonComponent::class],
    replaces = [AppModule::class]
)
object TestAppModule {

    @Provides
    @Singleton
    fun provideMockContext(): Context {
        return Mockito.mock(Context::class.java)
    }

    @Provides
    @Singleton
    fun provideMockNoteDatabase(): NoteDatabase {
        return Mockito.mock(NoteDatabase::class.java)
    }

    @Provides
    @Singleton
    @Named("notes_prefs")
    fun provideMockSharedPreferences(): SharedPreferences {
        return Mockito.mock(SharedPreferences::class.java)
    }

    @Provides
    @Singleton
    @Named("notes_prefs_encrypt")
    fun provideMockEncryptedSharedPreferences(): SharedPreferences {
        return Mockito.mock(SharedPreferences::class.java)
    }

    @Provides
    @Singleton
    @Named(RepositoryQualifiers.ROOM)
    fun provideMockRoomRepository(): NoteRepository {
        return Mockito.mock(NoteRepository::class.java)
    }

    @Provides
    @Singleton
    @Named(RepositoryQualifiers.SHARED)
    fun provideMockSharedPrefRepository(): NoteRepository {
        return Mockito.mock(NoteRepository::class.java)
    }

    @Provides
    @Singleton
    @Named(RepositoryQualifiers.ENCRYPTED_SHARED)
    fun provideMockEncryptedSharedPrefRepository(): NoteRepository {
        return Mockito.mock(NoteRepository::class.java)
    }

    @Provides
    @Singleton
    @Named(RepositoryQualifiers.FILE)
    fun provideMockFileRepository(): NoteRepository {
        return Mockito.mock(NoteRepository::class.java)
    }

    @Provides
    @Singleton
    @Named(RepositoryQualifiers.SCOPED)
    fun provideMockScopedStorageRepository(): NoteRepository {
        return Mockito.mock(NoteRepository::class.java)
    }

    @Provides
    @Singleton
    @Named(RepositoryQualifiers.KEY_STORE)
    fun provideMockKeyStoreRepository(): NoteRepository {
        return Mockito.mock(NoteRepository::class.java)
    }
}
