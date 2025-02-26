package com.mvi.notes.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.mvi.notes.RepositoryQualifiers
import com.mvi.notes.data.source.room.db.NoteDao
import com.mvi.notes.data.source.room.db.NoteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideNoteDatabase(@ApplicationContext context: Context): NoteDatabase =
        Room.databaseBuilder(context, NoteDatabase::class.java, "note_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideNoteDao(database: NoteDatabase): NoteDao = database.noteDao()

    @Provides
    @Singleton
    @Named(RepositoryQualifiers.SHARED)
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("notes_prefs", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    @Named(RepositoryQualifiers.ENCRYPTED_SHARED)
    fun provideEncryptedSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("notes_prefs_encrypt", Context.MODE_PRIVATE)

}
