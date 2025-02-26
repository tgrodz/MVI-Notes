package com.mvi.notes.data.source

import android.content.Context
import com.mvi.notes.data.NoteJsonConverter
import com.mvi.notes.data.source.file.NoteDataSource
import com.mvi.notes.domain.model.Note
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class FileNoteDataSourceTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockFilesDir: File  // Mock filesDir

    @Mock
    private lateinit var mockFile: File  // Mock actual file

    private lateinit var noteDataSource: NoteDataSource

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        mockFilesDir = mock(File::class.java)
        mockFile = mock(File::class.java)

        `when`(mockContext.filesDir).thenReturn(mockFilesDir)
        `when`(mockFilesDir.resolve(any<String>())).thenReturn(mockFile)
        `when`(mockFile.exists()).thenReturn(true)
        `when`(mockFile.readText()).thenReturn("[]")

        noteDataSource = NoteDataSource(mockContext)
        val fileField = NoteDataSource::class.java.getDeclaredField("file")
        fileField.isAccessible = true
        fileField.set(noteDataSource, mockFile)
    }

    @Test
    fun `addNote should save to file and update flow`() = runTest {
        `when`(mockFile.readText()).thenReturn("[]")
        noteDataSource.addNote("Test Title", "Test Content")
        val jsonCaptor = ArgumentCaptor.forClass(String::class.java)
        verify(mockFile).writeText(jsonCaptor.capture())
        assert(jsonCaptor.value.contains("Test Title"))
        assert(jsonCaptor.value.contains("Test Content"))
        val notes = noteDataSource.notesFlow.first()
        assertEquals(1, notes.size)
        assertEquals("Test Title", notes[0].title)
        assertEquals("Test Content", notes[0].text)
    }

    @Test
    fun `deleteNote should remove note from file`() = runTest {
        val note = Note(1, "Title", "Content", System.currentTimeMillis())
        `when`(mockFile.readText()).thenReturn(NoteJsonConverter.notesToJson(listOf(note)))
        noteDataSource.deleteNote(1)
        val jsonCaptor = ArgumentCaptor.forClass(String::class.java)
        verify(mockFile).writeText(jsonCaptor.capture())
        assertEquals("[]", jsonCaptor.value)
        val notes = noteDataSource.notesFlow.first()
        assertEquals(0, notes.size)
    }

    @Test
    fun `updateNote should modify existing note in file`() = runTest {
        val note = Note(1, "Old Title", "Old Content", System.currentTimeMillis())
        `when`(mockFile.readText()).thenReturn(NoteJsonConverter.notesToJson(listOf(note)))
        noteDataSource.updateNote(1, "New Title", "New Content")
        val jsonCaptor = ArgumentCaptor.forClass(String::class.java)
        verify(mockFile).writeText(jsonCaptor.capture())
        assert(jsonCaptor.value.contains("New Title"))
        assert(jsonCaptor.value.contains("New Content"))
        val notes = noteDataSource.notesFlow.first()
        assertEquals(1, notes.size)
        assertEquals("New Title", notes[0].title)
        assertEquals("New Content", notes[0].text)
    }

    @Test
    fun `loadNotes should read from file and return notes`() = runTest {
        val note = Note(1, "Saved Title", "Saved Content", System.currentTimeMillis())
        `when`(mockFile.readText()).thenReturn(NoteJsonConverter.notesToJson(listOf(note)))
        val notes = noteDataSource.notesFlow.first()
        assertEquals(1, notes.size)
        assertEquals("Saved Title", notes[0].title)
        assertEquals("Saved Content", notes[0].text)
    }

    @Test
    fun `readFromFile should return empty JSON if file does not exist`() = runTest {
        `when`(mockFile.exists()).thenReturn(false)
        val result = noteDataSource.notesFlow.first()
        assertEquals(0, result.size)
    }
}
