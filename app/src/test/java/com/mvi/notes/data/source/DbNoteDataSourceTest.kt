package com.mvi.notes.data.source

import com.mvi.notes.capture
import com.mvi.notes.data.source.room.NoteDataSource
import com.mvi.notes.data.source.room.db.NoteDao
import com.mvi.notes.data.source.room.db.NoteEntity
import com.mvi.notes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class DbNoteDataSourceTest {

    private lateinit var repository: NoteRepository

    @Mock
    private lateinit var noteDao: NoteDao

    @Captor
    private lateinit var noteCaptor: ArgumentCaptor<NoteEntity>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        doReturn(flowOf(emptyList<NoteEntity>())).`when`(noteDao).getAllNotes()
        repository = NoteDataSource(noteDao)
    }

    @Test
    fun addNote_shouldSaveNoteSuccessfully() = runBlocking {
        repository.addNote("Test Note", "Mocked Content")
        verify(noteDao).insertNote(capture(noteCaptor))
        val capturedNote = noteCaptor.value
        assertEquals("Test Note", capturedNote.title)
        assertEquals("Mocked Content", capturedNote.text)
    }

    @Test
    fun deleteNote_shouldRemoveNoteSuccessfully() = runBlocking {
        val noteId = 1
        repository.deleteNote(noteId)
        verify(noteDao).deleteNote(eq(noteId))
    }

    @Test
    fun updateNote_shouldModifyExistingNote() = runBlocking {
        repository.updateNote(1, "Updated Title", "Updated Content")
        verify(noteDao).updateNote(capture(noteCaptor))
        val capturedNote = noteCaptor.value
        assertEquals(1, capturedNote.id)
        assertEquals("Updated Title", capturedNote.title)
        assertEquals("Updated Content", capturedNote.text)
    }

    @Test
    fun getAllNotes_shouldReturnNotes(): Unit = runBlocking {
        val notes = listOf(
            NoteEntity(id = 1, title = "Note 1", text = "Content 1"),
            NoteEntity(id = 2, title = "Note 2", text = "Content 2")
        )
        doReturn(flowOf(notes)).`when`(noteDao).getAllNotes()
        repository = NoteDataSource(noteDao)
        val result = repository.notesFlow.first()
        assertEquals(2, result.size)
        assertEquals("Note 1", result[0].title)
        assertEquals("Note 2", result[1].title)
        verify(noteDao, times(2)).getAllNotes()
    }
}
