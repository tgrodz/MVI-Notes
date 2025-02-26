package com.mvi.notes.presentation.create

sealed class AddNoteState {
    data class Editing(val title: String = "", val text: String = "") : AddNoteState()
    object Saving : AddNoteState()
    object Saved : AddNoteState()
    data class Error(val message: String) : AddNoteState()
}