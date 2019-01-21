package me.uport.sdk.core

import android.support.annotation.VisibleForTesting
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Shorthand for the UI thread that is also a mockable context in unit tests
 */
val UI by lazy { coroutineUiContextInitBlock() }

private var coroutineUiContextInitBlock: () -> CoroutineContext = { Dispatchers.Main }

/**
 * Call this in @Before methods where you need to interact with UI context
 */
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
fun stubUiContext() {
    coroutineUiContextInitBlock = { EmptyCoroutineContext }
}