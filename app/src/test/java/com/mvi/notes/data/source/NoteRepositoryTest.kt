package com.mvi.notes.data.source

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import com.mvi.notes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/*
 Runs in a JVM environment (without Android OS)
 The test does not use real Context or Android APIs.
 Since SharedPreferences is an Android component, a unit test cannot use it directly.
 Mock work through Mockito
 */
class NoteRepositoryTest {

    @Mock
    private lateinit var sharedPreferences: SharedPreferences

    @Mock
    private lateinit var editor: Editor

    private lateinit var repository: NoteRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(sharedPreferences.edit()).thenReturn(editor)
        `when`(editor.putString(anyString(), anyString())).thenReturn(editor)
        `when`(editor.apply()).then {}

        repository = com.mvi.notes.data.source.sharedpref.NoteDataSource(sharedPreferences)
    }

    @Test
    fun `addNote should save note successfully`() = runBlocking {
        repository.addNote("Test Title", "Test Content")

        val notes = repository.notesFlow.first()
        assertEquals(1, notes.size)
        assertEquals("Test Title", notes[0].title)
        assertEquals("Test Content", notes[0].text)
    }

    @Test
    fun `deleteNote should remove note successfully`() = runBlocking {
        repository.addNote("Test Title", "Test Content")
        val addedNote = repository.notesFlow.first().first()

        repository.deleteNote(addedNote.id)

        val notes = repository.notesFlow.first()
        assertTrue(notes.isEmpty())
    }

    @Test
    fun `updateNote should modify existing note`() = runBlocking {
        repository.addNote("Old Title", "Old Content")
        val addedNote = repository.notesFlow.first().first()

        repository.updateNote(addedNote.id, "New Title", "New Content")

        val updatedNote = repository.notesFlow.first().first()
        assertEquals("New Title", updatedNote.title)
        assertEquals("New Content", updatedNote.text)
    }
}
