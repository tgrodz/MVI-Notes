package com.mvi.notes

import com.mvi.notes.domain.repository.NoteRepository
import com.mvi.notes.presentation.create.AddNoteIntent
import com.mvi.notes.presentation.create.AddNoteState
import com.mvi.notes.presentation.create.AddNoteViewModel
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
class AddNoteViewModelTest {

    private lateinit var viewModel: AddNoteViewModel

    @Mock
    private lateinit var repository: NoteRepository

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = AddNoteViewModel(repository)
    }

    @Test
    fun `testChangeTitleIntentUpdatesTitleInState`() = testScope.runBlockingTest {
        val newTitle = "New Title"
        viewModel.processIntent(AddNoteIntent.ChangeTitle(newTitle))
        val state = viewModel.state.first()
        assertTrue(state is AddNoteState.Editing)
        assertEquals(newTitle, (state as AddNoteState.Editing).title)
    }

    @Test
    fun `testChangeTextIntentUpdatesTextInState`() = testScope.runBlockingTest {
        val newText = "Note Text"
        viewModel.processIntent(AddNoteIntent.ChangeText(newText))
        val state = viewModel.state.first()
        assertTrue(state is AddNoteState.Editing)
        assertEquals(newText, (state as AddNoteState.Editing).text)
    }

    @Test
    fun `testSaveNoteIntentWithValidInputSavesNoteAndUpdatesStateToSaved`() = testScope.runBlockingTest {
        val title = "Valid Title"
        val text = "Valid Text"
        Mockito.doNothing().`when`(repository).addNote(title, text)
        viewModel.processIntent(AddNoteIntent.ChangeTitle(title))
        viewModel.processIntent(AddNoteIntent.ChangeText(text))
        viewModel.processIntent(AddNoteIntent.SaveNote)
        val state = viewModel.state.first()
        assertTrue(state is AddNoteState.Saved)
    }

    @Test
    fun `testSaveNoteIntentWithEmptyFieldsShowsErrorState`() = testScope.runBlockingTest {
        viewModel.processIntent(AddNoteIntent.ChangeTitle(""))
        viewModel.processIntent(AddNoteIntent.ChangeText(""))
        viewModel.processIntent(AddNoteIntent.SaveNote)
        val state = viewModel.state.first()
        assertTrue(state is AddNoteState.Error)
        assertEquals("Title and text cannot be empty", (state as AddNoteState.Error).message)
    }

    @Test
    fun `testSaveNoteIntentWithExceptionShowsErrorState`() = testScope.runBlockingTest {
        val title = "Valid Title"
        val text = "Valid Text"
        Mockito.doThrow(RuntimeException("Failed to save note"))
            .`when`(repository).addNote(title, text)
        viewModel.processIntent(AddNoteIntent.ChangeTitle(title))
        viewModel.processIntent(AddNoteIntent.ChangeText(text))
        viewModel.processIntent(AddNoteIntent.SaveNote)
        val state = viewModel.state.first()
        assertTrue(state is AddNoteState.Error)
        assertEquals("Failed to save note", (state as AddNoteState.Error).message)
    }
}
