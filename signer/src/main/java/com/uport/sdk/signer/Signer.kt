package com.uport.sdk.signer

import org.kethereum.functions.encodeRLP
import org.kethereum.model.SignatureData
import org.kethereum.model.Transaction
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Callback type for signature results.
 */
typealias SignatureCallback = (err: Exception?, sigData: SignatureData) -> Unit

/**
 * An interface used to sign transactions or messages for uport specific operations
 */
interface Signer {

    /**
     * Signs a blob of bytes that represent a RLP encoded transaction.
     */
    fun signETH(rawMessage: ByteArray, callback: SignatureCallback)

    /**
     * Signs a blob of bytes that represent the encoded header and payload parts of a JWT
     */
    fun signJWT(rawPayload: ByteArray, callback: SignatureCallback)

    /**
     * returns the ethereum address corresponding to the key that does the signing
     */
    fun getAddress(): String

    /**
     *
     */
    fun signRawTx(
            unsignedTx: Transaction,
            callback: (err: Exception?,
                       signedEncodedTransaction: ByteArray) -> Unit) = signETH(unsignedTx.encodeRLP())
    { err, sig ->
        if (err != null) {
            return@signETH callback(err, byteArrayOf())
        }
        return@signETH callback(null, unsignedTx.encodeRLP(sig))
    }

    companion object {
        /**
         * A useless signer that calls back with empty signature and has no associated address
         */
        val blank = object : Signer {
            override fun signETH(rawMessage: ByteArray, callback: SignatureCallback) = callback(null, SignatureData())
            override fun signJWT(rawPayload: ByteArray, callback: SignatureCallback) = callback(null, SignatureData())
            override fun getAddress(): String = ""
        }
    }
}


////////////////////////////////////////////////////
// signer extensions - wrap callbacks as coroutines
////////////////////////////////////////////////////

suspend fun Signer.signRawTx(unsignedTx: Transaction): ByteArray = suspendCoroutine { continuation ->
    this.signRawTx(unsignedTx) { err, signedEncodedTransaction ->
        if (err != null) {
            continuation.resumeWithException(err)
        } else {
            continuation.resume(signedEncodedTransaction)
        }
    }
}

suspend fun Signer.signETH(rawMessage: ByteArray): SignatureData = suspendCoroutine { continuation ->
    this.signETH(rawMessage) { err, sigData ->
        if (err != null) {
            continuation.resumeWithException(err)
        } else {
            continuation.resume(sigData)
        }
    }
}

suspend fun Signer.signJWT(rawMessage: ByteArray): SignatureData = suspendCoroutine { continuation ->
    this.signJWT(rawMessage) { err, sigData ->
        if (err != null) {
            continuation.resumeWithException(err)
        } else {
            continuation.resume(sigData)
        }
    }
}