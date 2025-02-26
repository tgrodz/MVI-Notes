package com.mvi.notes.presentation.all

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import com.mvi.notes.domain.model.Note
import com.mvi.notes.domain.model.SortOrder


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListOfNotesScreen(
    viewModel: NotesListViewModel,
    onNoteClick: (Note) -> Unit,
    onAddNoteClick: () -> Unit
) {
    val state = viewModel.state.collectAsState().value

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Notes", style = MaterialTheme.typography.headlineMedium) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNoteClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            SearchAndSortControls(viewModel, state)
            Spacer(modifier = Modifier.height(8.dp))
            NotesListContent(state, onNoteClick)
        }
    }
}

@Composable
private fun SearchAndSortControls(
    viewModel: NotesListViewModel,
    state: NotesListViewState
) {
    OutlinedTextField(
        value = state.searchQuery,
        onValueChange = { query ->
            viewModel.processIntent(NotesListIntent.SearchNotes(query))
        },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search by title...") },
        singleLine = true
    )
    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = {
            val newSortOrder = if (state.sortOrder == SortOrder.Ascending) {
                SortOrder.Descending
            } else {
                SortOrder.Ascending
            }
            viewModel.processIntent(NotesListIntent.ChangeSortOrder(newSortOrder))
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (state.sortOrder == SortOrder.Ascending)
                "Sort: Oldest First"
            else
                "Sort: Newest First"
        )
    }
}

@Composable
private fun NotesListContent(
    state: NotesListViewState,
    onNoteClick: (Note) -> Unit
) {
    when {
        state.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        state.error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${state.error}")
            }
        }
        state.notes.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No notes found")
            }
        }
        else -> {
            NotesListContainer(state.notes, onNoteClick)
        }
    }
}

@Composable
private fun NotesListContainer(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit
) {
    LazyColumn {
        items(notes) { note ->
            NoteListItem(note, onNoteClick)
        }
    }
}

@Composable
private fun NoteListItem(
    note: Note,
    onNoteClick: (Note) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onNoteClick(note) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(note.title, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(note.text, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Last updated: ${note.timestamp}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}