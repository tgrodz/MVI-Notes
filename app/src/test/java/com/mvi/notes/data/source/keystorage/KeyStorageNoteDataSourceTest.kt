package com.mvi.notes.data.source.keystorage

import android.content.Context
import android.content.SharedPreferences
import com.mvi.notes.data.source.encrypted.keystorage.NoteDataSource
import com.mvi.notes.data.source.encrypted.keystorage.KeyStoreHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class KeyStorageNoteDataSourceTest {

    @Mock
    private lateinit var keyStoreHelper: KeyStoreHelper

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var sharedPreferences: SharedPreferences

    @Mock
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var noteDataSource: NoteDataSource

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        whenever(context.getSharedPreferences(any(), any())).thenReturn(sharedPreferences)
        whenever(sharedPreferences.getString(any(), any())).thenReturn("[]")
        whenever(sharedPreferences.edit()).thenReturn(editor)
        whenever(editor.putString(any(), any())).thenReturn(editor)
        noteDataSource = NoteDataSource(keyStoreHelper, context)
        runTest { noteDataSource.notesFlow.first() }
    }

    @Test
    fun `addNote should encrypt and store note`() = runTest {
        whenever(keyStoreHelper.encryptData(any())).thenAnswer { "encrypted_" + it.arguments[0] }
        noteDataSource.addNote("Test Title", "Test Content")
        verify(editor).putString(eq("encrypted_notes"), any())
    }
}
