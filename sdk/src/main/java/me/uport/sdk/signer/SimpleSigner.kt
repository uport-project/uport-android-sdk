package me.uport.sdk.signer

import me.uport.sdk.core.Signer
import org.kethereum.crypto.ECKeyPair
import org.kethereum.crypto.Keys
import org.kethereum.extensions.hexToBigInteger
import org.kethereum.model.SignatureData

@Suppress("LiftReturnOrAssignment")
class SimpleSigner(private val privateKey: String) : Signer {

    override fun getAddress() = Keys.getAddress(ECKeyPair.create(privateKey.hexToBigInteger()))

    override fun signMessage(rawMessage: ByteArray, callback: (err: Exception?, sigData: SignatureData) -> Unit) {

        try {
            val keyPair = ECKeyPair.Companion.create(privateKey.hexToBigInteger())
            val sigData = org.kethereum.crypto.signMessage(rawMessage, keyPair)
            return callback(null, sigData)
        } catch (ex: Exception) {
            return callback(ex, SignatureData())
        }

    }

}