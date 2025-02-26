package com.mvi.notes.presentation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvi.notes.domain.repository.NoteRepository
import com.mvi.notes.RepositoryQualifiers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Named

@HiltViewModel
class AddNoteViewModel @Inject constructor(
    @Named(RepositoryQualifiers.IMPL) private val repository: NoteRepository
) : ViewModel() {

    private val _intents = MutableSharedFlow<AddNoteIntent>()
    private val _state = MutableStateFlow<AddNoteState>(AddNoteState.Editing())
    val state: StateFlow<AddNoteState> = _state.asStateFlow()

    init {
        observeIntents()
    }

    fun processIntent(intent: AddNoteIntent) {
        viewModelScope.launch {
            _intents.emit(intent)
        }
    }

    private fun observeIntents() {
        viewModelScope.launch {
            _intents.collect { intent ->
                when (intent) {
                    is AddNoteIntent.ChangeTitle -> updateTitle(intent.title)
                    is AddNoteIntent.ChangeText -> updateText(intent.text)
                    is AddNoteIntent.SaveNote -> saveNote()
                }
            }
        }
    }

    private fun updateTitle(newTitle: String) {
        updateState { it.copy(title = newTitle) }
    }

    private fun updateText(newText: String) {
        updateState { it.copy(text = newText) }
    }

    private fun updateState(update: (AddNoteState.Editing) -> AddNoteState) {
        val currentState = _state.value
        if (currentState is AddNoteState.Editing) {
            _state.value = update(currentState)
        }
    }

    private fun saveNote() {
        val currentState = _state.value
        if (currentState is AddNoteState.Editing) {
            if (currentState.title.isBlank() || currentState.text.isBlank()) {
                updateStateWithError("Title and text cannot be empty")
                return
            }
            _state.value = AddNoteState.Saving
            performSave(currentState.title, currentState.text)
        }
    }

    private fun performSave(title: String, text: String) {
        viewModelScope.launch {
            try {
                repository.addNote(title, text)
                _state.value = AddNoteState.Saved
            } catch (e: Exception) {
                updateStateWithError("Failed to save note")
            }
        }
    }

    private fun updateStateWithError(message: String) {
        _state.value = AddNoteState.Error(message)
    }
}
