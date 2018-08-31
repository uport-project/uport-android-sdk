package me.uport.sdk.signer

import me.uport.sdk.core.Signer
import org.kethereum.crypto.ECKeyPair
import org.kethereum.crypto.getAddress
import org.kethereum.crypto.signMessage
import org.kethereum.extensions.hexToBigInteger
import org.kethereum.model.SignatureData

class SimpleSigner(private val privateKey: String) : Signer {

    override fun getAddress() = ECKeyPair.create(privateKey.hexToBigInteger()).getAddress()

    override suspend fun signMessage(rawMessage: ByteArray): SignatureData {
        val keyPair = ECKeyPair.create(privateKey.hexToBigInteger())
        return keyPair.signMessage(rawMessage)
    }

}