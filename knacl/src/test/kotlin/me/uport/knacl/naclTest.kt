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
    fun `from secretKey`() {
        val secret = byteArrayOf(-37, 50, -122, -123, 0, -99, -50, -70, 78, -95, -24, -60, -7, -42, -56, -58, -13, 121, -81, -19, 48, 8, 71, 20, -99, 94, 58, -25, -25, -41, 18, -41)
        val expectedPub = byteArrayOf(-14, 21, -118, 55, 21, 37, -73, 40, 120, 119, 32, 67, 114, 14, 112, 43, 46, -94, -9, 45, 4, 35, -19, 11, 121, -86, -122, -1, 59, -107, -127, 122)
        val (pub, _) = nacl.boxKeyPairFromSecretKey(secret)
        assertArrayEquals(expectedPub, pub)
    }

    @Test
    fun `scalarmult base`() {
        val n = ByteArray(32)
        val p = ByteArray(32).apply { this[0] = 9 }
        val result = nacl.scalarMult(n, p)
        val expected = byteArrayOf(47, -27, 125, -93, 71, -51, 98, 67, 21, 40, -38, -84, 95, -69, 41, 7, 48, -1, -10, -124, -81, -60, -49, -62, -19, -112, -103, 95, 88, -53, 59, 116)
        assertArrayEquals(expected, result)
    }

    @Test
    fun scalarmult() {
        val aa = ByteArray(32) { (it and 0xff).toByte() }
        val bb = byteArrayOf(13, 121, -106, 0, -10, -1, -82, -30, -31, 33, -26, -72, -9, -96, 93, -58, 104, 116, -75, 29, -77, 16, 45, 13, 113, -9, -103, -96, -100, -76, -60, 97)
        val expected = byteArrayOf(-93, 89, 79, 51, 116, -107, 113, -26, 40, -85, 80, 51, -115, -8, 88, 66, -20, -119, -108, -76, 6, -93, -29, 103, 54, 45, -49, 61, 63, -68, -60, 18)

        val result = nacl.scalarMult(aa, bb)
        assertArrayEquals(expected, result)
    }

    @Test
    fun boxBefore() {
        val aa = ByteArray(32) { (it and 0xff).toByte() }
        val bsK = ByteArray(32) { ((32 - it) and 0xff).toByte() }
        val bb = nacl.boxKeyPairFromSecretKey(bsK).first
        val expected = byteArrayOf(-12, -14, 32, -95, 79, -108, -81, 87, -28, 107, 43, -80, -125, 67, -62, -25, -23, 4, 127, -44, 38, 63, -17, -43, 75, -43, 3, 69, 50, 80, 99, -20)
        val result = nacl.boxBefore(bb, aa)
        assertArrayEquals(expected, result)
    }


    @Test
    fun `secretbox with known inputs`() {
        val key = ByteArray(32) { (it and 0xff).toByte() }
        val nonce = ByteArray(24)
        val msg = byteArrayOf(104, 101, 108, 108, 111, 32, 98, 111, 98)
        val expected = arrayOf(153, 105, 253, 5, 217, 101, 111, 62, 75, 165, 94, 199, 110, 190, 255, 4, 34, 107, 156, 45, 247, 138, 221, 92, 62).map { it.toByte() }.toByteArray()

        val ciphertext1 = nacl.secretbox(msg, nonce, key)
        val ciphertext2 = TweetNaCl.secretbox(msg, nonce, key)
        assertArrayEquals(expected, ciphertext1)
        assertArrayEquals(expected, ciphertext2)
    }

    @Test
    fun `secretboxOpen with known inputs`() {
        val ciphertext = arrayOf(153, 105, 253, 5, 217, 101, 111, 62, 75, 165, 94, 199, 110, 190, 255, 4, 34, 107, 156, 45, 247, 138, 221, 92, 62).map { it.toByte() }.toByteArray()
        val key = ByteArray(32) { (it and 0xff).toByte() }
        val nonce = ByteArray(24)
        val expectedMsg = byteArrayOf(104, 101, 108, 108, 111, 32, 98, 111, 98)

        val msg = nacl.secretboxOpen(ciphertext, nonce, key)
        assertArrayEquals(expectedMsg, msg)
    }

    @Test
    fun `secretbox and secretboxOpen with known inputs`() {
        val key = ByteArray(32) { (it and 0xff).toByte() }
        val nonce = ByteArray(24)
        val msg = byteArrayOf(104, 101, 108, 108, 111, 32, 98, 111, 98)

        val ciphertext = nacl.secretbox(msg, nonce, key)
        val decrypted = nacl.secretboxOpen(ciphertext, nonce, key)
        assertArrayEquals(msg, decrypted)
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
    fun `box with known nonce`() {
        val nonce = ByteArray(nacl.crypto_box_NONCEBYTES)
        val skA = byteArrayOf(-44, -8, -39, -110, -71, -12, -81, -46, 122, -78, 39, 123, 15, 54, -45, 61, -114, 52, 49, -124, -72, 100, 37, 124, -91, -71, 84, 87, -75, -39, -112, -33)
        val pkB = byteArrayOf(94, -16, -16, 16, 41, -38, -42, -68, -127, 29, -70, -92, 117, 104, -28, -57, 56, -26, -63, 42, -46, -43, -74, 120, 40, -108, -30, 63, 120, -95, -83, 15)
        val msg = "hello knacl".toByteArray(Charsets.UTF_8)


        val result = nacl.box(msg, nonce, pkB, skA)

        val expectedResult = byteArrayOf(-115, -97, -75, 29, 21, 112, -26, 65, 25, -15, 95, -70, -90, -24, 34, 127, 32, -101, 126, 50, -3, -23, 88, 46, -42, -58, -16)
        assertArrayEquals(expectedResult, result)
    }


    @Test
    fun `box both directions`() {
        val nonce = byteArrayOf(-23, -83, 84, 51, 52, -50, 49, 7, 1, -47, 32, -60, -66, 51, -7, -96, 118, -60, 74, -127, -101, -37, -68, -59)
        val skA = byteArrayOf(-36, 102, -73, -6, 18, 63, 38, 48, 2, 94, -117, -108, -103, -128, 21, -2, 74, -29, -95, 112, -5, -102, 89, 12, 3, -118, 27, 38, -71, 110, -34, 59)
        val pkA = byteArrayOf(-85, 79, 49, -106, -93, -48, 89, -81, 12, -31, -94, 119, 94, -7, 6, -120, -19, -30, -20, 112, -82, -71, 80, -77, 81, -50, 73, 85, -65, -117, 0, 53)
        val skB = byteArrayOf(14, -123, 50, -46, -55, 115, 8, -6, -92, 43, -41, -27, -90, -109, -49, -22, 1, -77, 21, -24, -52, 10, 100, 110, 57, -53, 6, 66, -71, -42, -91, -83)
        val pkB = byteArrayOf(61, -70, -43, 21, -104, -14, 27, -103, -63, -30, 116, 123, 0, -56, -123, 56, -34, 111, -15, 58, 68, 32, -30, -93, 52, 69, 24, 5, 41, 62, -117, 49)
        val msg = "hello knacl".toByteArray(Charsets.UTF_8)

        val expected = byteArrayOf(-55, -16, -116, 75, 44, -122, 13, 59, 120, -63, 23, -34, 28, 97, -59, 12, -116, -21, -117, 8, 8, -104, 33, -56, -104, 102, 49)

        val msgForA = nacl.box(msg, nonce, pkA, skB)
        assertArrayEquals(expected, msgForA)

        val msgForB = nacl.box(msg, nonce, pkB, skA)
        assertArrayEquals(expected, msgForB)

    }

    @Test
    fun `box and boxOpen`() {
        val aliceSecretKey = ByteArray(32) { (it and 0xff).toByte() }
        val bobSecretKey = ByteArray(32) { ((32 - it) and 0xff).toByte() }

        val (alicePub, _) = nacl.boxKeyPairFromSecretKey(aliceSecretKey)
        val (bobPub, _) = nacl.boxKeyPairFromSecretKey(bobSecretKey)

        val nonce = ByteArray(nacl.crypto_box_NONCEBYTES)
        val msgToBob = byteArrayOf(104, 101, 108, 108, 111, 32, 98, 111, 98)

        val abBox = nacl.box(msgToBob, nonce, bobPub, aliceSecretKey)

        val bobReceived = nacl.boxOpen(abBox, nonce, alicePub, bobSecretKey)
        assertArrayEquals(msgToBob, bobReceived)
    }
}