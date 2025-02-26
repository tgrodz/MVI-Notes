package com.mvi.notes.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mvi.notes.domain.model.Note
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsNoteScreen(viewModel: NoteDetailsViewModel, note: Note, onBack: () -> Unit) {
    LaunchedEffect(note.id) {
        viewModel.setInitialNote(note)
    }
    val state = viewModel.state.collectAsState().value
    val scope = rememberCoroutineScope()

    // Navigate back when the note has been deleted.
    LaunchedEffect(state) {
        if (state is NoteDetailsState.Deleted) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Note", style = MaterialTheme.typography.headlineMedium) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            val titleValue = if (state is NoteDetailsState.Editing) state.title else ""
            val textValue = if (state is NoteDetailsState.Editing) state.text else ""

            OutlinedTextField(
                value = titleValue,
                onValueChange = { newTitle ->
                    if (state is NoteDetailsState.Editing) {
                        scope.launch {
                            viewModel.processIntent(
                                NoteDetailsIntent.ChangeTitle(
                                    state.id,
                                    newTitle
                                )
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Edit your title...") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = textValue,
                onValueChange = { newText ->
                    if (state is NoteDetailsState.Editing) {
                        scope.launch {
                            viewModel.processIntent(NoteDetailsIntent.ChangeText(state.id, newText))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = { Text("Edit your note...") },
                maxLines = Int.MAX_VALUE
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    if (state is NoteDetailsState.Editing) {
                        scope.launch {
                            viewModel.processIntent(
                                NoteDetailsIntent.UpdateNote(
                                    state.id,
                                    state.title,
                                    state.text
                                )
                            )
                        }
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (state is NoteDetailsState.Editing) {
                        scope.launch { viewModel.processIntent(NoteDetailsIntent.DeleteNote(state.id)) }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
            ) {
                Text("Delete Note")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
            if (state is NoteDetailsState.Saving) {
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator()
            }
            if (state is NoteDetailsState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

