package me.uport.sdk.core

import org.kethereum.functions.encodeRLP
import org.kethereum.model.SignatureData
import org.kethereum.model.Transaction

/**
 * An interface used to sign transactions or messages for uport specific operations
 */
interface Signer {
    suspend fun signMessage(rawMessage: ByteArray): SignatureData
    fun getAddress() : String

    suspend fun signRawTx(unsignedTx: Transaction): ByteArray {
        val sig = signMessage(unsignedTx.encodeRLP())
        return unsignedTx.encodeRLP(sig)
    }

    companion object {

        /**
         * A useless signer that calls back with empty signature and has no associated address
         */
        val blank = object : Signer {
            override suspend fun signMessage(rawMessage: ByteArray): SignatureData {
                return SignatureData()
            }

            override fun getAddress(): String = ""
        }
    }
}