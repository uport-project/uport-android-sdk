package me.uport.sdk.jwt

import assertk.assert
import assertk.assertions.isTrue
import org.junit.Test
import org.kethereum.crypto.model.PrivateKey
import org.kethereum.crypto.publicKeyFromPrivate
import org.kethereum.extensions.hexToBigInteger
import org.kethereum.hashes.sha256
import org.kethereum.model.SignatureData

class ECUtilsKtTest {

    @Test
    fun `can verify non recoverable JWT signature`() {

        val referencePayload = "Hello, world!".toByteArray()

        val referenceSignature = SignatureData(
                r = "6bcd81446183af193ca4a172d5c5c26345903b24770d90b5d790f74a9dec1f68".hexToBigInteger(),
                s = "e2b85b3c92c9b4f3cf58de46e7997d8efb6e14b2e532d13dfa22ee02f3a43d5d".hexToBigInteger()
        )

        val privateKey = PrivateKey("65fc670d9351cb87d1f56702fb56a7832ae2aab3427be944ab8c9f2a0ab87960".hexToBigInteger())
        val publicKey = publicKeyFromPrivate(privateKey)

        val messageHash = referencePayload.sha256()
        val result = ecVerify(messageHash, referenceSignature, publicKey)
        assert(result).isTrue()
    }
}