package com.mvi.notes.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.mvi.notes.domain.model.Note
import com.mvi.notes.presentation.all.ListOfNotesScreen
import com.mvi.notes.presentation.all.NotesListViewModel
import com.mvi.notes.presentation.create.AddNoteScreen
import com.mvi.notes.presentation.create.AddNoteViewModel
import com.mvi.notes.presentation.detail.DetailsNoteScreen
import com.mvi.notes.presentation.detail.NoteDetailsViewModel


@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Route.NotesScreen.route) {
        composable(Route.NotesScreen.route) {
            val listViewModel = hiltViewModel<NotesListViewModel>()
            ListOfNotesScreen(viewModel = listViewModel, onNoteClick = { note ->
                navController.navigate(Route.NoteDetailsScreen.createRoute(note))
            }, onAddNoteClick = {
                navController.navigate(Route.AddNoteScreen.route)
            })
        }

        composable(Route.AddNoteScreen.route) {
            val addNoteViewModel = hiltViewModel<AddNoteViewModel>()
            AddNoteScreen(viewModel = addNoteViewModel, onBack = { navController.popBackStack() })
        }

        composable(
            route = Route.NoteDetailsScreen.route,
            arguments = listOf(
                navArgument("noteId") { type = NavType.IntType },
                navArgument("noteTitle") { type = NavType.StringType },
                navArgument("noteText") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val note = Note(
                id = backStackEntry.arguments?.getInt("noteId") ?: 0,
                title = backStackEntry.arguments?.getString("noteTitle") ?: "",
                text = backStackEntry.arguments?.getString("noteText") ?: ""
            )
            val detailsViewModel = hiltViewModel<NoteDetailsViewModel>()
            DetailsNoteScreen(viewModel = detailsViewModel, note = note, onBack = { navController.popBackStack() })
        }
    }
}


