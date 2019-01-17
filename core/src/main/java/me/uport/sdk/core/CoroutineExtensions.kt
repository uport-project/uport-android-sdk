package me.uport.sdk.core

import android.support.annotation.VisibleForTesting
import kotlinx.coroutines.Dispatchers
import java.io.IOException
import kotlin.coroutines.*

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

/**
 * Suspend method that does a HTTP POST with a a [jsonBody] to the given [url]
 * and returns the response body as String or throws an Exception when failing
 * Takes an optional [authToken] that will be sent as `Bearer` token on an `Authorization` header
 *
 * @throws [IOException] if the request could not be executed due to cancellation, disconnect or timeout
 */
suspend fun urlPost(url: String, jsonBody: String, authToken: String? = null): String = suspendCoroutine { continuation ->
    urlPost(url, jsonBody, authToken) { err, count ->
        if (err != null) {
            continuation.resumeWithException(err)
        } else {
            continuation.resume(count)
        }
    }
}

/**
 * Suspend method that does a HTTP GET with given [url] and returns the response body as string or an exception
 * Takes an optional [authToken] that will be sent as `Bearer` token on an `Authorization` header
 *
 * @throws [IOException] if the request could not be executed due to cancellation, disconnect or timeout.
 */
suspend fun urlGet(url: String, authToken: String? = null): String = suspendCoroutine { continuation ->
    urlGet(url, authToken) { err, count ->
        if (err != null) {
            continuation.resumeWithException(err)
        } else {
            continuation.resume(count)
        }
    }
}