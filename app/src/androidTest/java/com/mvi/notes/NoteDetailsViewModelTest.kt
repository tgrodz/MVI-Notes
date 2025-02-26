package com.mvi.notes

import com.mvi.notes.domain.model.Note
import com.mvi.notes.domain.repository.NoteRepository
import com.mvi.notes.presentation.detail.NoteDetailsIntent
import com.mvi.notes.presentation.detail.NoteDetailsState
import com.mvi.notes.presentation.detail.NoteDetailsViewModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope


@OptIn(ExperimentalCoroutinesApi::class)
class NoteDetailsViewModelTest {

    private lateinit var viewModel: NoteDetailsViewModel

    @Mock
    private lateinit var repository: NoteRepository

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = NoteDetailsViewModel(repository)
    }

    @Test
    fun `testChangeTitleIntentUpdatesTitleInState`() = testScope.runBlockingTest {
        val newTitle = "New Note Title"
        val note = Note(1, "Initial Title", "Initial Text")
        viewModel.setInitialNote(note)
        viewModel.processIntent(NoteDetailsIntent.ChangeTitle(note.id, newTitle))
        val state = viewModel.state.first()
        assertTrue(state is NoteDetailsState.Editing)
        assertEquals(newTitle, (state as NoteDetailsState.Editing).title)
    }

    @Test
    fun `testChangeTextIntentUpdatesTextInState`() = testScope.runBlockingTest {
        val newText = "Updated note text"
        val note = Note(1, "Initial Title", "Initial Text")
        viewModel.setInitialNote(note)
        viewModel.processIntent(NoteDetailsIntent.ChangeText(note.id, newText))
        val state = viewModel.state.first()
        assertTrue(state is NoteDetailsState.Editing)
        assertEquals(newText, (state as NoteDetailsState.Editing).text)
    }

    @Test
    fun `testUpdateNoteIntentWithValidInputSavesTheNoteAndUpdatesState`() = testScope.runBlockingTest {
        val title = "Updated Title"
        val text = "Updated Note Text"
        val note = Note(1, "Initial Title", "Initial Text")
        viewModel.setInitialNote(note)
        Mockito.doNothing().`when`(repository).updateNote(note.id, title, text)
        viewModel.processIntent(NoteDetailsIntent.UpdateNote(note.id, title, text))
        val state = viewModel.state.first()
        assertTrue(state is NoteDetailsState.Editing)
        assertEquals(title, (state as NoteDetailsState.Editing).title)
        assertEquals(text, state.text)
    }

    @Test
    fun `testDeleteNoteIntentDeletesTheNoteAndUpdatesStateToDeleted`() = testScope.runBlockingTest {
        val note = Note(1, "Initial Title", "Initial Text")
        viewModel.setInitialNote(note)
        Mockito.doNothing().`when`(repository).deleteNote(note.id)
        viewModel.processIntent(NoteDetailsIntent.DeleteNote(note.id))
        val state = viewModel.state.first()
        assertTrue(state is NoteDetailsState.Deleted)
    }

    @Test
    fun `testUpdateNoteIntentWithErrorUpdatesStateToError`() = testScope.runBlockingTest {
        val title = "Updated Title"
        val text = "Updated Note Text"
        val note = Note(1, "Initial Title", "Initial Text")
        viewModel.setInitialNote(note)
        Mockito.doThrow(RuntimeException("Failed to update note")).`when`(repository).updateNote(note.id, title, text)
        viewModel.processIntent(NoteDetailsIntent.UpdateNote(note.id, title, text))
        val state = viewModel.state.first()
        assertTrue(state is NoteDetailsState.Error)
        assertEquals("Error saving note", (state as NoteDetailsState.Error).message)
    }

    @Test
    fun `testDeleteNoteIntentWithErrorUpdatesStateToError`() = testScope.runBlockingTest {
        val note = Note(1, "Initial Title", "Initial Text")
        viewModel.setInitialNote(note)
        Mockito.doThrow(RuntimeException("Failed to delete note")).`when`(repository).deleteNote(note.id)
        viewModel.processIntent(NoteDetailsIntent.DeleteNote(note.id))
        val state = viewModel.state.first()
        assertTrue(state is NoteDetailsState.Error)
        assertEquals("Error deleting note", (state as NoteDetailsState.Error).message)
    }
}

