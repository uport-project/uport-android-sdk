package me.uport.sdk.jwt

import android.support.annotation.VisibleForTesting
import android.support.annotation.VisibleForTesting.PROTECTED

/**
 * convenience method used during token processing.
 * Splits JWT into parts.
 * @throws IllegalArgumentException if it can't split or if the number of parts != 3
 */
@VisibleForTesting(otherwise = PROTECTED)
fun splitToken(token: String): Triple<String, String, String> {
    val parts: List<String>? = token.split('.', limit = 3)
    if (parts !== null && parts.size == 3) {
        return Triple(parts[0], parts[1], parts[2])
    } else {
        throw IllegalArgumentException("Token must have 3 parts: Header, Payload, and Signature")
    }
}