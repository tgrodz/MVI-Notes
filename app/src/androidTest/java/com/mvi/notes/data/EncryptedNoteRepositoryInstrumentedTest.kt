package com.mvi.notes.data

import com.mvi.notes.data.source.encrypted.EncryptedNoteDataSource
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.test.core.app.ApplicationProvider
import com.mvi.notes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/*
 It Uses Android Components (EncryptedSharedPreferences)!
 EncryptedSharedPreferences is part of Android's security-crypto library,
 which requires Android OS to function.
*/
class EncryptedNoteRepositoryInstrumentedTest {

    private lateinit var repository: NoteRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            "test_encrypted_notes_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPreferences.edit().clear().commit()
        repository = EncryptedNoteDataSource(sharedPreferences)
    }

    @Test
    fun addNote_shouldSaveDataSecurely() = runBlocking {
        repository.addNote("Encrypted Title", "This is a secret note")
        val notes = repository.notesFlow.first()
        assertEquals(1, notes.size)
        assertEquals("Encrypted Title", notes[0].title)
        assertEquals("This is a secret note", notes[0].text)
    }

    @Test
    fun deleteNote_shouldRemoveDataSecurely() = runBlocking {
        repository.addNote("Sensitive", "Encrypted Data")
        val addedNote = repository.notesFlow.first().first()

        repository.deleteNote(addedNote.id)
        val notes = repository.notesFlow.first()
        assertTrue(notes.isEmpty())
    }

    @Test
    fun getNote_shouldRetrieveCorrectNote() = runBlocking {
        repository.addNote("First Note", "This is the first note")
        repository.addNote("Second Note", "This is the second note")

        val notes = repository.notesFlow.first()
        assertEquals(2, notes.size)

        val firstNote = notes.first { it.title == "First Note" }
        assertNotNull(firstNote)
        assertEquals("First Note", firstNote.title)
        assertEquals("This is the first note", firstNote.text)
    }
}
