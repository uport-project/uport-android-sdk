package com.uport.sdk.signer

import org.kethereum.crypto.*
import org.kethereum.crypto.model.PrivateKey
import org.kethereum.extensions.hexToBigInteger
import org.kethereum.hashes.sha256
import org.kethereum.model.SignatureData

/**
 * Simple [Signer] implementation that holds the KeyPair in memory.
 *
 * There is no special handling of threads for callbacks.
 */
class KPSigner(privateKey: String) : Signer {

    private val keyPair = PrivateKey(privateKey.hexToBigInteger()).toECKeyPair()

    override fun signJWT(rawPayload: ByteArray, callback: (err: Exception?, sigData: SignatureData) -> Unit) {
        try {
            val sigData = signMessageHash(rawPayload.sha256(), keyPair, false)
            callback(null, sigData)
        } catch (err: Exception) {
            callback(err, SignatureData())
        }
    }

    override fun getAddress() = keyPair.toAddress().hex

    override fun signETH(rawMessage: ByteArray, callback: (err: Exception?, sigData: SignatureData) -> Unit) {

        try {
            val sigData = keyPair.signMessage(rawMessage)
            callback(null, sigData)
        } catch (ex: Exception) {
            callback(ex, SignatureData())
        }

    }

}