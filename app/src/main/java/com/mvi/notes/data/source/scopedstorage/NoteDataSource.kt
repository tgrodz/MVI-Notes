package com.mvi.notes.data.source.scopedstorage

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.mvi.notes.data.NoteJsonConverter
import com.mvi.notes.domain.model.Note
import com.mvi.notes.domain.repository.NoteRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) : NoteRepository {

    private val fileName = "scope_notes.json"
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
        writeToScopedStorage(jsonString)
        _notesFlow.value = notes
    }

    private fun loadNotes(): List<Note> {
        val jsonString = readFromScopedStorage() ?: "[]"
        return NoteJsonConverter.jsonToNotes(jsonString)
    }

    private fun writeToScopedStorage(data: String) {
        try {
            val resolver: ContentResolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
                put(MediaStore.Files.FileColumns.MIME_TYPE, "application/json")
                put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
            }

            val uri: Uri? = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(data.toByteArray())
                    outputStream.flush()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun readFromScopedStorage(): String? {
        try {
            val resolver: ContentResolver = context.contentResolver
            val uri: Uri? = findFileUri(fileName)

            uri?.let {
                resolver.openInputStream(it)?.use { inputStream ->
                    return inputStream.bufferedReader().use { it.readText() }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun findFileUri(fileName: String): Uri? {
        val resolver: ContentResolver = context.contentResolver
        val projection = arrayOf(MediaStore.Files.FileColumns._ID)
        val selection = "${MediaStore.Files.FileColumns.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(fileName)

        val cursor = resolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val idColumnIndex = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val id = it.getLong(idColumnIndex)
                return Uri.withAppendedPath(MediaStore.Files.getContentUri("external"), id.toString())
            }
        }
        return null
    }
}
