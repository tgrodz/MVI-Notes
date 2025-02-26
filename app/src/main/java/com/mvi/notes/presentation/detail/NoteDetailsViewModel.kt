package com.mvi.notes.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvi.notes.domain.model.Note
import com.mvi.notes.domain.repository.NoteRepository
import com.mvi.notes.RepositoryQualifiers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class NoteDetailsViewModel @Inject constructor(
    @Named(RepositoryQualifiers.IMPL) private val repository: NoteRepository
) : ViewModel() {

    private val _state = MutableStateFlow<NoteDetailsState>(NoteDetailsState.Editing(0, "", ""))
    val state: StateFlow<NoteDetailsState> = _state.asStateFlow()

    private val _intents = MutableSharedFlow<NoteDetailsIntent>()

    init {
        observeIntents()
    }

    fun setInitialNote(note: Note) {
        _state.value = NoteDetailsState.Editing(note.id, note.title, note.text)
    }

    fun processIntent(intent: NoteDetailsIntent) {
        viewModelScope.launch {
            _intents.emit(intent)
        }
    }

    private fun observeIntents() {
        viewModelScope.launch {
            _intents.collect { intent ->
                when (intent) {
                    is NoteDetailsIntent.ChangeText -> updateText(intent.text)
                    is NoteDetailsIntent.ChangeTitle -> updateTitle(intent.title)
                    is NoteDetailsIntent.UpdateNote -> updateNote(intent.id, intent.title, intent.text)
                    is NoteDetailsIntent.DeleteNote -> deleteNoteById(intent.id)
                }
            }
        }
    }

    private fun updateText(newText: String) {
        updateState { currentState ->
            currentState.copy(text = newText)
        }
    }

    private fun updateTitle(newTitle: String) {
        updateState { currentState ->
            currentState.copy(title = newTitle)
        }
    }

    private fun updateState(update: (NoteDetailsState.Editing) -> NoteDetailsState) {
        if (_state.value is NoteDetailsState.Editing) {
            _state.value = update(_state.value as NoteDetailsState.Editing)
        }
    }

    private fun updateNote(id: Int, title: String, text: String) {
        _state.value = NoteDetailsState.Saving
        viewModelScope.launch {
            try {
                repository.updateNote(id, title, text)
                _state.value = NoteDetailsState.Editing(id, title, text)
            } catch (e: Exception) {
                _state.value = NoteDetailsState.Error("Error saving note: ${e.message}")
            }
        }
    }

    private fun deleteNoteById(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteNote(id)
                _state.value = NoteDetailsState.Deleted
            } catch (e: Exception) {
                _state.value = NoteDetailsState.Error("Error deleting note: ${e.message}")
            }
        }
    }
}
