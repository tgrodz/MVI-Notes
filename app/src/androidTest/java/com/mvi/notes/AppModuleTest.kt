package com.mvi.notes

import android.content.Context
import android.content.SharedPreferences
import com.mvi.notes.data.source.encrypted.keystorage.KeyStoreHelper
import com.mvi.notes.data.source.room.db.NoteDatabase
import com.mvi.notes.domain.repository.NoteRepository
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import javax.inject.Named
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

@HiltAndroidTest
class AppModuleTest {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var noteDatabase: NoteDatabase

    @Inject
    @Named("notes_prefs")
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    @Named("notes_prefs_encrypt")
    lateinit var encryptedSharedPreferences: SharedPreferences

    @Inject
    @Named(RepositoryQualifiers.ROOM)
    lateinit var roomRepository: NoteRepository

    @Inject
    @Named(RepositoryQualifiers.SHARED)
    lateinit var sharedPrefRepository: NoteRepository

    @Inject
    @Named(RepositoryQualifiers.ENCRYPTED_SHARED)
    lateinit var encryptedSharedPrefRepository: NoteRepository

    @Inject
    @Named(RepositoryQualifiers.FILE)
    lateinit var fileRepository: NoteRepository

    @Inject
    @Named(RepositoryQualifiers.SCOPED)
    lateinit var scopedStorageRepository: NoteRepository

    @Inject
    @Named(RepositoryQualifiers.KEY_STORE)
    lateinit var keyStoreRepository: NoteRepository

    @Test
    fun testContextInjection() {
        assertNotNull(context)
    }

    @Test
    fun testNoteDatabaseInjection() {
        assertNotNull(noteDatabase)
    }

    @Test
    fun testSharedPreferencesInjection() {
        assertNotNull(sharedPreferences)
    }

    @Test
    fun testEncryptedSharedPreferencesInjection() {
        assertNotNull(encryptedSharedPreferences)
    }

    @Test
    fun testRoomRepositoryInjection() {
        assertNotNull(roomRepository)
    }

    @Test
    fun testSharedPrefRepositoryInjection() {
        assertNotNull(sharedPrefRepository)
    }

    @Test
    fun testEncryptedSharedPrefRepositoryInjection() {
        assertNotNull(encryptedSharedPrefRepository)
    }

    @Test
    fun testFileRepositoryInjection() {
        assertNotNull(fileRepository)
    }

    @Test
    fun testScopedStorageRepositoryInjection() {
        assertNotNull(scopedStorageRepository)
    }

    @Test
    fun testKeyStoreRepositoryInjection() {
        assertNotNull(keyStoreRepository)
    }

    @Test
    fun testKeyStoreHelper() {
        val keyStoreHelper = KeyStoreHelper()
        keyStoreHelper.generateKey()
        assertTrue(keyStoreHelper.isKeyGenerated())
    }
}
