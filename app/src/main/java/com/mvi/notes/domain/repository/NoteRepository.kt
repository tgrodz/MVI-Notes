package com.mvi.notes.domain.repository
import com.mvi.notes.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    val notesFlow: Flow<List<Note>>
    suspend fun addNote(title: String, text: String)
    suspend fun deleteNote(id: Int)
    suspend fun updateNote(id: Int, title: String, text: String)
}
