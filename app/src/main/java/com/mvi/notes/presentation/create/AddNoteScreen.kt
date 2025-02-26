package com.mvi.notes.presentation.create

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(viewModel: AddNoteViewModel, onBack: () -> Unit) { // Changed parameter type here
    val state = viewModel.state.collectAsState().value
    val scope = rememberCoroutineScope()

    // When note is saved, navigate back.
    LaunchedEffect(state) {
        if (state is AddNoteState.Saved) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add New Note", style = MaterialTheme.typography.headlineMedium) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            val titleValue = if (state is AddNoteState.Editing) state.title else ""
            val textValue = if (state is AddNoteState.Editing) state.text else ""

            OutlinedTextField(
                value = titleValue,
                onValueChange = { newTitle ->
                    scope.launch { viewModel.processIntent(AddNoteIntent.ChangeTitle(newTitle)) }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter title...") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = textValue,
                onValueChange = { newText ->
                    scope.launch { viewModel.processIntent(AddNoteIntent.ChangeText(newText)) }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter note...") },
                maxLines = 5
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { scope.launch { viewModel.processIntent(AddNoteIntent.SaveNote) } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Note")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", style = MaterialTheme.typography.bodyLarge)
            }
            if (state is AddNoteState.Saving) {
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator()
            }
            if (state is AddNoteState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
