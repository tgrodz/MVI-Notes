package com.mvi.notes.data.room

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.mvi.notes.data.source.room.db.NoteDao
import com.mvi.notes.data.source.room.db.NoteDatabase
import com.mvi.notes.data.source.room.db.NoteEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class NoteDaoTest {

    private lateinit var database: NoteDatabase
    private lateinit var noteDao: NoteDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, NoteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        noteDao = database.noteDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertNote_shouldAddNoteSuccessfully() = runBlocking {
        val note = NoteEntity(title = "Test Note", text = "This is a test")

        noteDao.insertNote(note)

        val notes = noteDao.getAllNotes().first()
        assertEquals(1, notes.size)
        assertEquals("Test Note", notes[0].title)
        assertEquals("This is a test", notes[0].text)
    }

    @Test
    fun deleteNote_shouldRemoveNoteSuccessfully() = runBlocking {
        val note = NoteEntity(id = 1, title = "Test", text = "To be deleted")
        noteDao.insertNote(note)

        noteDao.deleteNote(1)

        val notes = noteDao.getAllNotes().first()
        assertTrue(notes.isEmpty())
    }

    @Test
    fun updateNote_shouldModifyExistingNote() = runBlocking {
        val note = NoteEntity(id = 1, title = "Original", text = "Original text")
        noteDao.insertNote(note)

        val updatedNote = NoteEntity(id = 1, title = "Updated", text = "Updated text")
        noteDao.updateNote(updatedNote)

        val notes = noteDao.getAllNotes().first()
        assertEquals("Updated", notes[0].title)
        assertEquals("Updated text", notes[0].text)
    }
}
