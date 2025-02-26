package com.mvi.notes.presentation.create

sealed class AddNoteIntent {
    data class ChangeTitle(val title: String) : AddNoteIntent()
    data class ChangeText(val text: String) : AddNoteIntent()
    object SaveNote : AddNoteIntent()
}
