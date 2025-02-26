package com.mvi.notes.data.source.room

import com.mvi.notes.data.source.room.db.NoteDao
import com.mvi.notes.data.source.room.db.NoteEntity
import com.mvi.notes.domain.model.Note
import com.mvi.notes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteDataSource @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override val notesFlow: Flow<List<Note>> = noteDao.getAllNotes().map { entities ->
        entities.map { it.toDomainModel() }
    }

    override suspend fun addNote(title: String, text: String) {
        val noteEntity = NoteEntity(title = title, text = text)
        noteDao.insertNote(noteEntity)
    }

    override suspend fun deleteNote(id: Int) {
        noteDao.deleteNote(id)
    }

    override suspend fun updateNote(id: Int, title: String, text: String) {
        val noteEntity = NoteEntity(id = id, title = title, text = text, timestamp = System.currentTimeMillis())
        noteDao.updateNote(noteEntity)
    }
}


fun NoteEntity.toDomainModel(): Note {
    return Note(id = this.id, title = this.title, text = this.text, timestamp = this.timestamp)
}
