package com.mvi.notes.data

import com.mvi.notes.domain.model.Note
import org.json.JSONArray
import org.json.JSONObject

object NoteJsonConverter {

    fun notesToJson(notes: List<Note>): String {
        val jsonArray = JSONArray()
        notes.forEach { note ->
            jsonArray.put(noteToJson(note))
        }
        return jsonArray.toString()
    }

    fun jsonToNotes(jsonString: String): List<Note> {
        val jsonArray = JSONArray(jsonString)
        val notes = mutableListOf<Note>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            notes.add(jsonToNote(jsonObject))
        }
        return notes
    }

    fun noteToJson(note: Note): JSONObject {
        return JSONObject().apply {
            put("id", note.id)
            put("title", note.title)
            put("text", note.text)
            put("timestamp", note.timestamp)
        }
    }

    fun jsonToNote(jsonObject: JSONObject): Note {
        return Note(
            id = jsonObject.getInt("id"),
            title = jsonObject.optString("title", "Untitled Note"),
            text = jsonObject.getString("text"),
            timestamp = jsonObject.optLong("timestamp", System.currentTimeMillis())
        )
    }
}
