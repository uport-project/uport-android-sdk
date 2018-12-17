package me.uport.sdk.core.experimental

import me.uport.sdk.core.urlGet
import me.uport.sdk.core.urlPost
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * wraps a [urlPost] in a suspend function
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
 * wraps a [urlGet] in a suspend function
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