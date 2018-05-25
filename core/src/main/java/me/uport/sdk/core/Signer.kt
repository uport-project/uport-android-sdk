package me.uport.sdk.core

import org.kethereum.functions.encodeRLP
import org.kethereum.model.SignatureData
import org.kethereum.model.Transaction
import kotlin.coroutines.experimental.suspendCoroutine

/**
 * An interface used to sign transactions or messages for uport specific operations
 */
interface Signer {

    fun signMessage(rawMessage: ByteArray, callback: (err: Exception?, sigData: SignatureData) -> Unit)

    fun getAddress() : String

    fun signRawTx(
            unsignedTx: Transaction,
            callback: (err: Exception?,
                       signedEncodedTransaction: ByteArray) -> Unit) = signMessage(unsignedTx.encodeRLP())
    { err, sig ->
        if (err != null) {
            return@signMessage callback(err, byteArrayOf())
        }
        return@signMessage callback(null, unsignedTx.encodeRLP(sig))
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

suspend fun Signer.signMessage(rawMessage: ByteArray): SignatureData = suspendCoroutine { continuation ->
    this.signMessage(rawMessage) { err, sigData ->
        if (err != null) {
            continuation.resumeWithException(err)
        } else {
            continuation.resume(sigData)
        }
    }
}