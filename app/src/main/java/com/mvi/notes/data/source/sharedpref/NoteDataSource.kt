package com.mvi.notes.data.source.sharedpref

import android.content.SharedPreferences
import com.mvi.notes.RepositoryQualifiers
import com.mvi.notes.data.NoteJsonConverter
import com.mvi.notes.domain.model.Note
import com.mvi.notes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton


@Singleton
class NoteDataSource @Inject constructor(
    @Named(RepositoryQualifiers.SHARED) private val sharedPreferences: SharedPreferences
) : NoteRepository {

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
            if (it.id == id) it.copy(title = title, text = text, timestamp = System.currentTimeMillis()) else it
        }
        saveNotes(newList)
    }

    private fun saveNotes(notes: List<Note>) {
        val jsonString = NoteJsonConverter.notesToJson(notes)
        sharedPreferences.edit().putString("notes", jsonString).apply()
        _notesFlow.value = notes
    }

    private fun loadNotes(): List<Note> {
        val jsonString = sharedPreferences.getString("notes", "[]") ?: "[]"
        return NoteJsonConverter.jsonToNotes(jsonString)
    }
}
