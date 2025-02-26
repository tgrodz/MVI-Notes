package com.mvi.notes

import androidx.activity.compose.setContent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.navigation.testing.TestNavHostController
import com.mvi.notes.MainActivity
import com.mvi.notes.navigation.NavGraph
import com.mvi.notes.navigation.Route
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var navController: TestNavHostController

    @Before
    fun setupNavHost() {
        composeTestRule.activity.setContent {
            navController = TestNavHostController(composeTestRule.activity)
            NavGraph(navController = navController)
        }
    }

    @Test
    fun testNavigationToAddNoteScreen() {
        composeTestRule.onNodeWithContentDescription("Add Note").performClick()

        assert(navController.currentDestination?.route == Route.AddNoteScreen.route)
    }
}
