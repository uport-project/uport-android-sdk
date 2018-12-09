package me.uport.knacl

import org.junit.Assert.*
import org.junit.Test

@ExperimentalUnsignedTypes
class NaClTestQuick {

    @Test
    fun `all setup is OK`() {
        assertTrue(true)
    }

    @Test
    fun `secretbox and secretboxOpen`() {
        val key = ByteArray(nacl.crypto_secretbox_KEYBYTES) { (it and 0xff).toByte() }
        val nonce = ByteArray(nacl.crypto_secretbox_NONCEBYTES) { ((32 + it) and 0xff).toByte() }
        val msg = "message to encrypt".toByteArray(Charsets.UTF_8)
        val box = nacl.secretbox(msg, nonce, key)
        val openedMsg = nacl.secretboxOpen(box, nonce, key)!!
        assertArrayEquals(msg, openedMsg)
    }

    @Test
    fun `secretboxOpen with invalid box`() {
        val key = ByteArray(nacl.crypto_secretbox_KEYBYTES)
        val nonce = ByteArray(nacl.crypto_secretbox_NONCEBYTES)
        assertNull(nacl.secretboxOpen(ByteArray(0), nonce, key))
        assertNull(nacl.secretboxOpen(ByteArray(10), nonce, key))
        assertNull(nacl.secretboxOpen(ByteArray(100), nonce, key))
    }

    @Test
    fun `secretboxOpen with invalid nonce`() {
        val key = ByteArray(nacl.crypto_secretbox_KEYBYTES)
        val nonce = ByteArray(nacl.crypto_secretbox_NONCEBYTES) { (it and 0xff).toByte() }
        for (i in 0 until nonce.size) {

        }
        val msg = "message to encrypt".toByteArray(Charsets.UTF_8)
        val box = nacl.secretbox(msg, nonce, key)
        assertArrayEquals(msg, nacl.secretboxOpen(box, nonce, key))
        nonce[0] = 255.toByte()
        assertNull(nacl.secretboxOpen(box, nonce, key))
    }

    @Test
    fun `secretboxOpen with invalid key`() {
        val key = ByteArray(nacl.crypto_secretbox_KEYBYTES) { (it and 0xff).toByte() }
        val nonce = ByteArray(nacl.crypto_secretbox_NONCEBYTES)
        val msg = "message to encrypt".toByteArray(Charsets.UTF_8)
        val box = nacl.secretbox(msg, nonce, key)
        assertArrayEquals(msg, nacl.secretboxOpen(box, nonce, key))
        key[0] = 255.toByte()
        assertNull(nacl.secretboxOpen(box, nonce, key))
    }

    @Test
    fun `secretbox with lengths of 0 to 1024`() {
        val key = ByteArray(nacl.crypto_secretbox_KEYBYTES) { (it and 0xff).toByte() }
        val nonce = ByteArray(nacl.crypto_secretbox_NONCEBYTES)
        val fullMsg = ByteArray(1024) { (it and 0xff).toByte() }

        for (i in 0 until fullMsg.size) {
            val msg = fullMsg.copyOfRange(0, i)
            val box = nacl.secretbox(msg, nonce, key)
            val unbox = nacl.secretboxOpen(box, nonce, key)
            assertArrayEquals(msg, unbox)
        }
    }

    @Test
    fun `box and boxOpen`() {
        val aliceSecretKey = ByteArray(32) { (it and 0xff).toByte() }
        val bobSecretKey = ByteArray(32) { ((32 - it) and 0xff).toByte() }

        val (alicePub, aliceSecret) = nacl.boxKeyPairFromSecretKey(aliceSecretKey)
        val (bobPub, bobSecret) = nacl.boxKeyPairFromSecretKey(bobSecretKey)

        val nonce = ByteArray(nacl.crypto_box_NONCEBYTES)
        val msg = "message to encrypt".toByteArray(Charsets.UTF_8)

        val cipherToBob = nacl.box(msg, nonce, bobPub, aliceSecret)
        val bobReceived = nacl.boxOpen(cipherToBob, nonce, alicePub, bobSecret)
        assertArrayEquals(msg, bobReceived)
    }
}