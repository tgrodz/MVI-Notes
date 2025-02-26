package com.mvi.notes.presentation.detail

sealed class NoteDetailsIntent {
    data class ChangeText(val id: Int, val text: String) : NoteDetailsIntent()
    data class ChangeTitle(val id: Int, val title: String) : NoteDetailsIntent()
    data class UpdateNote(val id: Int, val title: String, val text: String) : NoteDetailsIntent()
    data class DeleteNote(val id: Int) : NoteDetailsIntent()
}
