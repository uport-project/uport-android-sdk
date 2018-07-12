package me.uport.sdk.signer

import me.uport.sdk.core.Signer
import org.kethereum.crypto.ECKeyPair
import org.kethereum.crypto.getAddress
import org.kethereum.crypto.signMessage
import org.kethereum.extensions.hexToBigInteger
import org.kethereum.model.SignatureData

class SimpleSigner(private val privateKey: String) : Signer {

    override fun getAddress() = ECKeyPair.create(privateKey.hexToBigInteger()).getAddress()

    override fun signMessage(rawMessage: ByteArray, callback: (err: Exception?, sigData: SignatureData) -> Unit) {

        try {
            val keyPair = ECKeyPair.create(privateKey.hexToBigInteger())
            val sigData = keyPair.signMessage(rawMessage)
            callback(null, sigData)
        } catch (ex: Exception) {
            callback(ex, SignatureData())
        }

    }

}