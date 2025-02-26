package com.mvi.notes.data.source.encrypted.keystorage

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mvi.notes.domain.model.Note
import com.mvi.notes.domain.repository.NoteRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton


/**
The Keystore does not store the data itself (such as notes, passwords, etc.), but only encryption keys.

The **Keystore** is located in a secure environment on devices with
a hardware security module (TEE – Trusted Execution Environment), such as Secure Enclave or StrongBox.
If hardware encryption is not available, the keys are stored in a software Keystore,
which is still protected.

Root access allows modifications to the user part of the OS but does not grant control over the TEE – Trusted Execution Environment.
*/

@Singleton
class NoteDataSource @Inject constructor(
    private val keyStoreRepository: KeyStoreHelper,
    @ApplicationContext private val context: Context
) : NoteRepository {

    companion object {
        private const val PREF_NAME = "secure_notes_prefs"
        private const val KEY_NOTES = "encrypted_notes"
        private const val TAG = "NoteDataSource"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREF_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _notesFlow = MutableStateFlow(loadNotes())
    override val notesFlow: Flow<List<Note>> get() = _notesFlow.asStateFlow()

    override suspend fun addNote(title: String, text: String) {
        val newNote = Note(
            id = if (_notesFlow.value.isEmpty()) 1 else _notesFlow.value.maxOf { it.id } + 1,
            title = title,
            text = text
        )
        val newList = _notesFlow.value.toMutableList().apply { add(newNote) }
        saveNotes(newList)
    }

    override suspend fun deleteNote(id: Int) {
        val newList = _notesFlow.value.filterNot { it.id == id }
        saveNotes(newList)
    }

    override suspend fun updateNote(id: Int, title: String, text: String) {
        val newList = _notesFlow.value.map { note ->
            if (note.id == id) note.copy(title = title, text = text)
            else note
        }
        saveNotes(newList)
    }

    private fun saveNotes(notes: List<Note>) {
        val encryptedNotes = notes.map { note ->
            val encryptedTitle = keyStoreRepository.encryptData(note.title)
            val encryptedText = keyStoreRepository.encryptData(note.text)
            note.copy(title = encryptedTitle, text = encryptedText)
        }
        val jsonString = Gson().toJson(encryptedNotes)
        sharedPreferences.edit().putString(KEY_NOTES, jsonString).apply()
        _notesFlow.value = notes
    }

    private fun loadNotes(): List<Note> {
        val jsonString = sharedPreferences.getString(KEY_NOTES, "[]") ?: "[]"
        val type = object : TypeToken<List<Note>>() {}.type
        val encryptedNotes: List<Note> = Gson().fromJson(jsonString, type) ?: emptyList()

        val decryptedNotes = encryptedNotes.map { note ->
            try {
                val decryptedTitle = keyStoreRepository.decryptData(note.title)
                val decryptedText = keyStoreRepository.decryptData(note.text)
                note.copy(title = decryptedTitle, text = decryptedText)
            } catch (e: Exception) {
                note.copy(title = "Decryption error", text = "Decryption error")
            }
        }
        Log.d(TAG, "Loaded ${decryptedNotes.size} decrypted note(s)")
        return decryptedNotes
    }
}
