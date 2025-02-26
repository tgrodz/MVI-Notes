package com.mvi.notes

import android.Manifest
import android.app.Instrumentation
import android.content.pm.PackageManager
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE)

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun testStoragePermissionGranted() {
        scenario.onActivity { activity ->
            val permissionStatus = activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            assert(permissionStatus == PackageManager.PERMISSION_GRANTED)
        }
    }

    @Test
    fun testStoragePermissionDenied() {
        scenario.onActivity { activity ->
            activity.requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
            val permissionStatus = activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            assert(permissionStatus != PackageManager.PERMISSION_GRANTED)
        }
    }

}
