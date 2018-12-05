package com.uport.sdk.signer

import org.junit.Assert.assertEquals
import org.junit.Test

class ExtensionsKtTest {

    @Test
    fun `can pack ciphertext and iv`() {
        val data = listOf("iv", "cipher").map { it.toByteArray() }.toTypedArray()
        val packed = packCiphertext(*data)
        val (iv, cipher) = unpackCiphertext(packed)
        assertEquals("cipher", String(cipher))
        assertEquals("iv", String(iv))
    }

    @Test
    fun `can pack empty iv and ciphertext`() {
        val data = arrayOf(byteArrayOf(), "cipher".toByteArray())
        val packed = packCiphertext(*data)
        val (iv, cipher) = unpackCiphertext(packed)
        assertEquals("cipher", String(cipher))
        assertEquals("", String(iv))
    }

    @Test
    fun `can pack only ciphertext`() {
        val data = arrayOf("cipher".toByteArray())
        val packed = packCiphertext(*data)
        val (cipher) = unpackCiphertext(packed)
        assertEquals("cipher", String(cipher))
    }
}