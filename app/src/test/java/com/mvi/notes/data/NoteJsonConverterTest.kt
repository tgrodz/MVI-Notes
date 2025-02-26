package com.mvi.notes.data

import com.mvi.notes.domain.model.Note
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteJsonConverterTest {

    @Test
    fun testNoteToJsonAndBack() {
        val originalNote = Note(
            id = 1,
            title = "Test Note",
            text = "Some test content",
            timestamp = 123456789L
        )
        val json = NoteJsonConverter.noteToJson(originalNote)
        val convertedNote = NoteJsonConverter.jsonToNote(json)
        assertEquals(originalNote, convertedNote)
    }

    @Test
    fun testNotesListToJsonAndBack() {
        val notes = listOf(
            Note(id = 1, title = "Note One", text = "Content one", timestamp = 111L),
            Note(id = 2, title = "Note Two", text = "Content two", timestamp = 222L)
        )
        val jsonString = NoteJsonConverter.notesToJson(notes)
        val convertedNotes = NoteJsonConverter.jsonToNotes(jsonString)
        assertEquals(notes, convertedNotes)
    }

    @Test
    fun testJsonToNoteWithMissingTitle() {
        val json = JSONObject().apply {
            put("id", 3)
            put("text", "Content without title")
            put("timestamp", 333L)
        }
        val note = NoteJsonConverter.jsonToNote(json)
        assertEquals(3, note.id)
        assertEquals("Untitled Note", note.title)
        assertEquals("Content without title", note.text)
        assertEquals(333L, note.timestamp)
    }
}
