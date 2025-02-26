package com.mvi.notes.data.source.file

import android.content.Context
import com.mvi.notes.data.NoteJsonConverter
import com.mvi.notes.domain.model.Note
import com.mvi.notes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.*
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException

@Singleton
class NoteDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) : NoteRepository {

    private val fileName = "simple_notes.json"
    private val file: File by lazy {
        File(context.filesDir, fileName)  // Internal app storage
    }

    private val _notesFlow = MutableStateFlow(loadNotes())
    override val notesFlow: Flow<List<Note>> get() = _notesFlow.asStateFlow()

    override suspend fun addNote(title: String, text: String) {
        val newList = _notesFlow.value.toMutableList().apply {
            add(
                Note(
                    id = if (isEmpty()) 1 else maxOf { it.id } + 1,
                    title = title,
                    text = text,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        saveNotes(newList)
    }

    override suspend fun deleteNote(id: Int) {
        val newList = _notesFlow.value.filterNot { it.id == id }
        saveNotes(newList)
    }

    override suspend fun updateNote(id: Int, title: String, text: String) {
        val newList = _notesFlow.value.map {
            if (it.id == id) it.copy(title = title, text = text, timestamp = System.currentTimeMillis())
            else it
        }
        saveNotes(newList)
    }

    private fun saveNotes(notes: List<Note>) {
        val jsonString = NoteJsonConverter.notesToJson(notes)
        writeToFile(jsonString)
        _notesFlow.value = notes
    }

    private fun loadNotes(): List<Note> {
        val jsonString = readFromFile() ?: "[]"
        return NoteJsonConverter.jsonToNotes(jsonString)
    }

    private fun writeToFile(data: String) {
        try {
            file.writeText(data)
            Log.d("NoteDataSource", "Data saved to file successfully")
        } catch (e: IOException) {
            Log.e("NoteDataSource", "Error saving data to file", e)
        }
    }

    private fun readFromFile(): String? {
        return try {
            if (file.exists()) {
                file.readText()
            } else {
                Log.w("NoteDataSource", "File not found, returning empty JSON")
                null
            }
        } catch (e: IOException) {
            Log.e("NoteDataSource", "Error reading data from file", e)
            null
        }
    }
}
