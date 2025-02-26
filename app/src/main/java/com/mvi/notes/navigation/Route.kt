package com.mvi.notes.navigation

import com.mvi.notes.domain.model.Note

sealed class Route(val route: String) {
    object NotesScreen : Route("notes_list")
    object AddNoteScreen : Route("add_note")

    object NoteDetailsScreen : Route("note_details/{noteId}/{noteTitle}/{noteText}") {
        fun createRoute(note: Note) = "note_details/${note.id}/${note.title}/${note.text}"
    }
}