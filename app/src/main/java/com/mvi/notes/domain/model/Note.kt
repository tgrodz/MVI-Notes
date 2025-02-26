package com.mvi.notes.domain.model

data class Note(
    val id: Int,
    val title: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
