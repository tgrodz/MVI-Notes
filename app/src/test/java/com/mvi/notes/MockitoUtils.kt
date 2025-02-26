package com.mvi.notes
import org.mockito.ArgumentCaptor

/**
 * Utility function to simplify capturing arguments in Mockito verifications.
 */
fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()