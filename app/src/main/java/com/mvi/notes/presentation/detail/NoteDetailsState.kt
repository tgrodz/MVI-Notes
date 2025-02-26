package com.mvi.notes.presentation.detail

sealed class NoteDetailsState {
    data class Editing(val id: Int, val title: String, val text: String) : NoteDetailsState()
    object Saving : NoteDetailsState()
    data class Error(val message: String) : NoteDetailsState()
    object Deleted : NoteDetailsState()
}
