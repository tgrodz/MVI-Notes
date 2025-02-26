package com.mvi.notes

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mvi.notes.domain.model.Note
import com.mvi.notes.domain.model.SortOrder
import com.mvi.notes.domain.repository.NoteRepository
import com.mvi.notes.presentation.all.NotesListIntent
import com.mvi.notes.presentation.all.NotesListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class NotesListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var repository: NoteRepository

    private lateinit var viewModel: NotesListViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = NotesListViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun processIntentAddNote_shouldCallRepositoryAddNote() = runTest {
        // Given
        val title = "Test Title"
        val text = "Test Text"
        // When
        viewModel.processIntent(NotesListIntent.AddNote(title, text))
        advanceUntilIdle()
        // Then
        verify(repository).addNote(title, text)
    }

    @Test
    fun processIntentDeleteNote_shouldCallRepositoryDeleteNote() = runTest {
        // Given
        val id = 1
        // When
        viewModel.processIntent(NotesListIntent.DeleteNote(id))
        advanceUntilIdle()
        // Then
        verify(repository).deleteNote(id)
    }

    @Test
    fun processIntentUpdateNote_shouldCallRepositoryUpdateNote() = runTest {
        // Given
        val id = 1
        val title = "Updated Title"
        val text = "Updated Text"
        // When
        viewModel.processIntent(NotesListIntent.UpdateNote(id, title, text))
        advanceUntilIdle()
        // Then
        verify(repository).updateNote(id, title, text)
    }

    @Test
    fun processIntentSearchNotes_shouldUpdateSearchQueryInState() = runTest {
        // Given
        val query = "Search Query"
        // When
        viewModel.processIntent(NotesListIntent.SearchNotes(query))
        advanceUntilIdle()
        // Then
        assert(viewModel.state.value.searchQuery == query)
    }

    @Test
    fun processIntentChangeSortOrder_shouldUpdateSortOrderInState() = runTest {
        // Given
        val order = SortOrder.Ascending
        // When
        viewModel.processIntent(NotesListIntent.ChangeSortOrder(order))
        advanceUntilIdle()
        // Then
        assert(viewModel.state.value.sortOrder == order)
    }

    @Test
    fun observeNotes_shouldUpdateStateWithFilteredAndSortedNotes() = runTest {
        // Given
        val notes = listOf(
            Note(1, "Title 1", "Text 1", 1000),
            Note(2, "Title 2", "Text 2", 2000)
        )
        `when`(repository.notesFlow).thenReturn(flowOf(notes))
        // When
        viewModel.processIntent(NotesListIntent.SearchNotes("Title"))
        viewModel.processIntent(NotesListIntent.ChangeSortOrder(SortOrder.Ascending))
        advanceUntilIdle()
        // Then
        assert(viewModel.state.value.notes == notes.sortedBy { it.timestamp })
    }
}