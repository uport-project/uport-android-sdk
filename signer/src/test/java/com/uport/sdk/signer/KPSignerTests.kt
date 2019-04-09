package com.uport.sdk.signer

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.kethereum.extensions.hexToBigInteger
import org.kethereum.model.SignatureData
import org.walleth.khex.hexToByteArray

class KPSignerTests {

    @Test
    fun `sign ETH calls back without error`() {

        val expectedSignature = SignatureData(
                r = "809e3b5ef25f4a3b039139e2fb70f70b636eba89c77a3b01e0c71c1a36d84126".hexToBigInteger(),
                s = "38524dfcd3e412cb6bc37f4594bbad104b6764bb14c64e42c699730106d1885a".hexToBigInteger(),
                v = 28.toByte())

        val rawTransactionBytes = "f380850ba43b7400832fefd8949e2068cce22de4e1e80f15cb71ef435a20a3b37c880de0b6b3a7640000890abcdef01234567890".hexToByteArray()

        val signer = KPSigner("3686e245890c7f997766b73a21d8e59f6385e1208831af3862574790cbc3d158")

        signer.signETH(rawTransactionBytes) { err, sigData ->
            assert(err).isNull()
            assert(sigData).isEqualTo(expectedSignature)
        }
    }

    @Test
    fun `signJWT calls back with correct signature`() {
        val referencePayload = "Hello, world!".toByteArray()

        val referenceSignature = SignatureData(
                r = "6bcd81446183af193ca4a172d5c5c26345903b24770d90b5d790f74a9dec1f68".hexToBigInteger(),
                s = "e2b85b3c92c9b4f3cf58de46e7997d8efb6e14b2e532d13dfa22ee02f3a43d5d".hexToBigInteger(),
                v = 28.toByte())

        val signer = KPSigner("65fc670d9351cb87d1f56702fb56a7832ae2aab3427be944ab8c9f2a0ab87960")

        signer.signJWT(referencePayload) { err, sigData ->
            assert(err).isNull()
            assert(sigData).isEqualTo(referenceSignature)
        }
    }

    @Test
    fun `signETH coroutine passes`() = runBlocking {

        val expectedSignature = SignatureData(
                r = "809e3b5ef25f4a3b039139e2fb70f70b636eba89c77a3b01e0c71c1a36d84126".hexToBigInteger(),
                s = "38524dfcd3e412cb6bc37f4594bbad104b6764bb14c64e42c699730106d1885a".hexToBigInteger(),
                v = 28.toByte())

        val rawTransactionBytes = "f380850ba43b7400832fefd8949e2068cce22de4e1e80f15cb71ef435a20a3b37c880de0b6b3a7640000890abcdef01234567890".hexToByteArray()

        val signer = KPSigner("3686e245890c7f997766b73a21d8e59f6385e1208831af3862574790cbc3d158")
        val sigData = signer.signETH(rawTransactionBytes)

        assert(sigData).isEqualTo(expectedSignature)
    }

    @Test
    fun `signJWT coroutine passes`() = runBlocking {
        val referencePayload = "Hello, world!".toByteArray()

        val referenceSignature = SignatureData(
                r = "6bcd81446183af193ca4a172d5c5c26345903b24770d90b5d790f74a9dec1f68".hexToBigInteger(),
                s = "e2b85b3c92c9b4f3cf58de46e7997d8efb6e14b2e532d13dfa22ee02f3a43d5d".hexToBigInteger(),
                v = 28.toByte())

        val signer = KPSigner("65fc670d9351cb87d1f56702fb56a7832ae2aab3427be944ab8c9f2a0ab87960")
        val sigData = signer.signJWT(referencePayload)
        assert(sigData).isEqualTo(referenceSignature)
    }

    @Test
    fun `returned address is non null`() {
        val signer = KPSigner("65fc670d9351cb87d1f56702fb56a7832ae2aab3427be944ab8c9f2a0ab87960")
        assert(signer.getAddress()).isEqualTo("0x794adde0672914159c1b77dd06d047904fe96ac8")
    }
}