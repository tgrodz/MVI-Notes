package com.mvi.notes.presentation.all

import com.mvi.notes.domain.model.Note
import com.mvi.notes.domain.model.SortOrder

data class NotesListViewState(
    val notes: List<Note> = emptyList(),
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.Descending,
    val isLoading: Boolean = true,
    val error: String? = null
)
