package com.mvi.notes.presentation.all

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvi.notes.domain.model.SortOrder
import com.mvi.notes.domain.repository.NoteRepository
import com.mvi.notes.RepositoryQualifiers
import com.mvi.notes.domain.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class NotesListViewModel @Inject constructor(
    @Named(RepositoryQualifiers.IMPL) private val repository: NoteRepository
) : ViewModel() {

    private val _intents = MutableSharedFlow<NotesListIntent>()
    private val _state = MutableStateFlow(NotesListViewState())
    open val state: StateFlow<NotesListViewState> = _state.asStateFlow()

    init {
        observeNotes()
        handleIntents()
    }

    fun processIntent(intent: NotesListIntent) {
        viewModelScope.launch {
            _intents.emit(intent)
        }
    }

    private fun handleIntents() {
        viewModelScope.launch {
            _intents.collect { intent ->
                when (intent) {
                    is NotesListIntent.AddNote -> addNote(intent)
                    is NotesListIntent.DeleteNote -> deleteNote(intent)
                    is NotesListIntent.UpdateNote -> updateNote(intent)
                    is NotesListIntent.SearchNotes -> searchNotes(intent)
                    is NotesListIntent.ChangeSortOrder -> changeSortOrder(intent)
                }
            }
        }
    }

    private fun addNote(intent: NotesListIntent.AddNote) {
        viewModelScope.launch {
            repository.addNote(intent.title, intent.text)
        }
    }

    private fun deleteNote(intent: NotesListIntent.DeleteNote) {
        viewModelScope.launch {
            repository.deleteNote(intent.id)
        }
    }

    private fun updateNote(intent: NotesListIntent.UpdateNote) {
        viewModelScope.launch {
            repository.updateNote(intent.id, intent.title, intent.text)
        }
    }

    private fun searchNotes(intent: NotesListIntent.SearchNotes) {
        updateState { it.copy(searchQuery = intent.query, isLoading = true) }
    }

    private fun changeSortOrder(intent: NotesListIntent.ChangeSortOrder) {
        updateState { it.copy(sortOrder = intent.order, isLoading = true) }
    }

    private fun updateState(update: (NotesListViewState) -> NotesListViewState) {
        _state.update { currentState -> update(currentState) }
    }

    private fun observeNotes() {
        viewModelScope.launch {
            combine(
                repository.notesFlow,
                _state.map { it.searchQuery }.distinctUntilChanged(),
                _state.map { it.sortOrder }.distinctUntilChanged()
            ) { notes, query, sortOrder ->
                filterAndSortNotes(notes, query, sortOrder)
            }
                .catch { throwable -> handleError(throwable) }
                .collect { filteredNotes -> updateStateWithNotes(filteredNotes) }
        }
    }

    private fun filterAndSortNotes(notes: List<Note>, query: String, sortOrder: SortOrder): List<Note> {
        return notes
            .filter { it.title.contains(query, ignoreCase = true) }
            .sortedBy { if (sortOrder == SortOrder.Ascending) it.timestamp else -it.timestamp }
    }

    private fun handleError(throwable: Throwable) {
        _state.update { it.copy(error = throwable.localizedMessage, isLoading = false) }
    }

    private fun updateStateWithNotes(filteredNotes: List<Note>) {
        _state.update { currentState ->
            currentState.copy(notes = filteredNotes, isLoading = false, error = null)
        }
    }
}
