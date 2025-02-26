package com.mvi.notes.presentation.all

import com.mvi.notes.domain.model.SortOrder

sealed class NotesListIntent {
    data class AddNote(val title: String, val text: String) : NotesListIntent()
    data class DeleteNote(val id: Int) : NotesListIntent()
    data class UpdateNote(val id: Int, val title: String, val text: String) : NotesListIntent()
    data class SearchNotes(val query: String) : NotesListIntent()
    data class ChangeSortOrder(val order: SortOrder) : NotesListIntent()
}