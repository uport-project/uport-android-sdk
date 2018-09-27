package me.uport.sdk.signer

import me.uport.sdk.core.Signer
import org.kethereum.crypto.ECKeyPair
import org.kethereum.crypto.getAddress
import org.kethereum.crypto.signMessage
import org.kethereum.crypto.signMessageHash
import org.kethereum.extensions.hexToBigInteger
import org.kethereum.hashes.sha256
import org.kethereum.model.SignatureData

/**
 * Simple [Signer] implementation that holds its keys in memory.
 *
 * There is no special handling of threads for callbacks.
 */
class SimpleSigner(private val privateKey: String) : Signer {

    override fun signJWT(rawPayload: ByteArray, callback: (err: Exception?, sigData: SignatureData) -> Unit) {
        try {
            val keyPair = ECKeyPair.create(privateKey.hexToBigInteger())
            val sigData = signMessageHash(rawPayload.sha256(), keyPair, false)
            callback(null, sigData)
        } catch (err: Exception) {
            callback(err, SignatureData())
        }
    }

    override fun getAddress() = ECKeyPair.create(privateKey.hexToBigInteger()).getAddress()

    override fun signETH(rawMessage: ByteArray, callback: (err: Exception?, sigData: SignatureData) -> Unit) {

        try {
            val keyPair = ECKeyPair.create(privateKey.hexToBigInteger())
            val sigData = keyPair.signMessage(rawMessage)
            callback(null, sigData)
        } catch (ex: Exception) {
            callback(ex, SignatureData())
        }

    }

}