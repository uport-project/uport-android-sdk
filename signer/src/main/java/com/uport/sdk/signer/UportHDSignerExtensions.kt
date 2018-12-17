package com.uport.sdk.signer

import android.content.Context
import com.uport.sdk.signer.encryption.KeyProtection
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 *
 * Exposes some HD key provider async methods as coroutines
 */

suspend fun UportHDSigner.createHDSeed(
        context: Context,
        level: KeyProtection.Level): Pair<String, String> = suspendCoroutine {

    this.createHDSeed(context, level) { err, address, pubKeyBase64 ->
        if (err != null) {
            it.resumeWithException(err)
        } else {
            it.resume(address to pubKeyBase64)
        }
    }
}

suspend fun UportHDSigner.importHDSeed(
        context: Context,
        level: KeyProtection.Level,
        phrase: String): Pair<String, String> = suspendCoroutine {

    this.importHDSeed(context, level, phrase) { err, address, pubKeyBase64 ->
        if (err != null) {
            it.resumeWithException(err)
        } else {
            it.resume(address to pubKeyBase64)
        }
    }
}

/**
 * Extension function that wraps the `computeAddressForPath` as a coroutine
 */
suspend fun UportHDSigner.computeAddressForPath(
        context: Context,
        rootAddress: String,
        derivationPath: String,
        prompt: String): Pair<String, String> = suspendCoroutine {

    this.computeAddressForPath(context, rootAddress, derivationPath, prompt) { err, address, pubKeyBase64 ->
        if (err != null) {
            it.resumeWithException(err)
        } else {
            it.resume(address to pubKeyBase64)
        }
    }
}

suspend fun UportHDSigner.showHDSeed(
        context: Context,
        rootAddress: String,
        prompt: String): String = suspendCoroutine {

    this.showHDSeed(context, rootAddress, prompt) { err, phrase ->
        if (err != null) {
            it.resumeWithException(err)
        } else {
            it.resume(phrase)
        }
    }
}