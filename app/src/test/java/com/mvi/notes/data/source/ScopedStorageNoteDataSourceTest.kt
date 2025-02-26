package com.mvi.notes.data.source

import com.mvi.notes.data.source.file.NoteDataSource

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.mvi.notes.data.NoteJsonConverter
import com.mvi.notes.domain.model.Note
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.*
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ScopedStorageNoteDataSourceTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockContentResolver: ContentResolver

    @Mock
    private lateinit var mockUri: Uri

    @Mock
    private lateinit var mockCursor: Cursor

    private lateinit var noteDataSource: NoteDataSource

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(mockContext.contentResolver).thenReturn(mockContentResolver)
        mockStatic(MediaStore.Files::class.java).use { mediaStoreMock ->
            mediaStoreMock.`when`<Uri> {
                MediaStore.Files.getContentUri("external")
            }.thenReturn(mockUri)
        }

        noteDataSource = NoteDataSource(mockContext)
    }

    @Test
    fun `addNote should save to Scoped Storage`() = runTest {
        `when`(mockContentResolver.insert(any(), any())).thenReturn(mockUri)
        noteDataSource.addNote("Test Title", "Test Content")
        val contentValuesCaptor = argumentCaptor<ContentValues>()
        verify(mockContentResolver).insert(eq(mockUri), contentValuesCaptor.capture())
        assertEquals("application/json", contentValuesCaptor.firstValue.getAsString(MediaStore.Files.FileColumns.MIME_TYPE))
        assertEquals(Environment.DIRECTORY_DOCUMENTS, contentValuesCaptor.firstValue.getAsString(MediaStore.Files.FileColumns.RELATIVE_PATH))
        val notes = noteDataSource.notesFlow.first()
        assertEquals(1, notes.size)
        assertEquals("Test Title", notes[0].title)
        assertEquals("Test Content", notes[0].text)
    }

    @Test
    fun `deleteNote should remove note from Scoped Storage`() = runTest {
        val note = Note(1, "Title", "Content", System.currentTimeMillis())
        `when`(mockContentResolver.query(any(), any(), any(), any(), any())).thenReturn(mockCursor)
        `when`(mockCursor.moveToFirst()).thenReturn(true)
        `when`(mockCursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)).thenReturn(0)
        `when`(mockCursor.getLong(0)).thenReturn(123L)
        noteDataSource.deleteNote(1)
        val uriCaptor = argumentCaptor<Uri>()
        verify(mockContentResolver).delete(uriCaptor.capture(), any(), any())
        assert(uriCaptor.firstValue.toString().contains("123"))
        val notes = noteDataSource.notesFlow.first()
        assertEquals(0, notes.size)
    }

    @Test
    fun `updateNote should modify existing note in Scoped Storage`() = runTest {
        val note = Note(1, "Old Title", "Old Content", System.currentTimeMillis())
        `when`(mockContentResolver.query(any(), any(), any(), any(), any())).thenReturn(mockCursor)
        `when`(mockCursor.moveToFirst()).thenReturn(true)
        `when`(mockCursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)).thenReturn(0)
        `when`(mockCursor.getLong(0)).thenReturn(123L)
        noteDataSource.updateNote(1, "New Title", "New Content")
        val contentValuesCaptor = argumentCaptor<ContentValues>()
        verify(mockContentResolver).update(eq(mockUri), contentValuesCaptor.capture(), any(), any())
        assert(contentValuesCaptor.firstValue.getAsString(MediaStore.Files.FileColumns.DISPLAY_NAME)!!.contains("scope_notes.json"))
        val notes = noteDataSource.notesFlow.first()
        assertEquals(1, notes.size)
        assertEquals("New Title", notes[0].title)
        assertEquals("New Content", notes[0].text)
    }

    @Test
    fun `loadNotes should read from Scoped Storage`() = runTest {
        val note = Note(1, "Saved Title", "Saved Content", System.currentTimeMillis())
        `when`(mockContentResolver.query(any(), any(), any(), any(), any())).thenReturn(mockCursor)
        `when`(mockCursor.moveToFirst()).thenReturn(true)
        `when`(mockCursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)).thenReturn(0)
        `when`(mockCursor.getLong(0)).thenReturn(123L)
        val inputStream = note.toString().byteInputStream()
        `when`(mockContentResolver.openInputStream(any())).thenReturn(inputStream)
        val notes = noteDataSource.notesFlow.first()
        assertEquals(1, notes.size)
        assertEquals("Saved Title", notes[0].title)
        assertEquals("Saved Content", notes[0].text)
    }

    @Test
    fun `readFromScopedStorage should return empty JSON if file not found`() = runTest {
        `when`(mockContentResolver.query(any(), any(), any(), any(), any())).thenReturn(null)
        val result = noteDataSource.notesFlow.first()
        assertEquals(0, result.size)
    }
}
