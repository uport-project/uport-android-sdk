package me.uport.sdk.transport

import me.uport.knacl.nacl
import me.uport.sdk.core.decodeBase64
import me.uport.sdk.core.padBase64
import me.uport.sdk.core.toBase64
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.walleth.khex.hexToByteArray
import kotlin.math.ceil
import kotlin.math.roundToInt


class CryptoTest {

    private val ASYNC_ENC_ALGORITHM = "x25519-xsalsa20-poly1305"
    private val BLOCK_SIZE = 64

    fun String.pad(): String {
        val paddedSize = (ceil(length.toDouble() / BLOCK_SIZE) * BLOCK_SIZE).roundToInt()
        return this.padEnd(paddedSize, '\u0000')
    }

    fun String.unpad(): String = this.replace("\u0000+$".toRegex(), "")

    @Test
    fun `zero padding`() {
        val xx = "hello".pad()
        println(xx)
        val hh = xx.unpad()
        println(hh)
    }

    @Test
    fun `encrypt and decrypt`() {
        val msg = "Hello EIP1098"
        val bobSecretKey = ByteArray(32) { it.toByte() }
        val tested = Crypto()
        val bobPublicKeyBase64 = tested.getEncryptionPublicKey(bobSecretKey).toBase64().padBase64()
        val enc = tested.encryptMessage(msg, bobPublicKeyBase64)
        val recoveredMessage = tested.decryptMessage(enc, bobSecretKey)
        assertEquals(msg, recoveredMessage)
    }

    @Test
    fun `decrypt EIP1098 message`() {
        val c = Crypto.EncryptedMessage(
                nonce = "1dvWO7uOnBnO7iNDJ9kO9pTasLuKNlej",
                ephemPublicKey = "FBH1/pAEHOOW14Lu3FWkgV3qOEcuL78Zy+qW1RwzMXQ=",
                ciphertext = "f8kBcl/NCyf3sybfbwAKk/np2Bzt9lRVkZejr6uh5FgnNlH/ic62DZzy")
        val decryptedMessage = Crypto().decryptMessage(c, "7e5374ec2ef0d91761a6e72fdf8f6ac665519bfdf6da0a2329cf0d804514b816".hexToByteArray())
        assertEquals("My name is Satoshi Buterin", decryptedMessage)
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
    fun `box with known nonce`() {
        val nonce = ByteArray(nacl.crypto_box_NONCEBYTES)
        val skA = byteArrayOf(-44, -8, -39, -110, -71, -12, -81, -46, 122, -78, 39, 123, 15, 54, -45, 61, -114, 52, 49, -124, -72, 100, 37, 124, -91, -71, 84, 87, -75, -39, -112, -33)
        val pkB = byteArrayOf(94, -16, -16, 16, 41, -38, -42, -68, -127, 29, -70, -92, 117, 104, -28, -57, 56, -26, -63, 42, -46, -43, -74, 120, 40, -108, -30, 63, 120, -95, -83, 15)
        val msg = "hello knacl".toByteArray(Charsets.UTF_8)


        val result = nacl.box(msg, nonce, pkB, skA)
        println(skA.toBase64().padBase64())
        println(pkB.toBase64().padBase64())
        println(nonce.toBase64().padBase64())
        println(result.toBase64().padBase64())

        val expectedResult = "jZ+1HRVw5kEZ8V+6pugifyCbfjL96Vgu1sbw".decodeBase64()
        println(expectedResult)
        assertArrayEquals(expectedResult, result)
    }

    @Test
    fun `box with fixed input`() {
        val nonce = "6a1UMzTOMQcB0SDEvjP5oHbESoGb27zF".decodeBase64()
        val skA = "3Ga3+hI/JjACXouUmYAV/krjoXD7mlkMA4obJrlu3js=".decodeBase64()
        val pkA = "q08xlqPQWa8M4aJ3XvkGiO3i7HCuuVCzUc5JVb+LADU=".decodeBase64()
        val skB = "DoUy0slzCPqkK9flppPP6gGzFejMCmRuOcsGQrnWpa0=".decodeBase64()
        val pkB = "PbrVFZjyG5nB4nR7AMiFON5v8TpEIOKjNEUYBSk+izE=".decodeBase64()
        val msg = "hello knacl".toByteArray(Charsets.UTF_8)


        val result = nacl.box(msg, nonce, pkA, skB)
        println(skA.toBase64().padBase64())
        println(pkB.toBase64().padBase64())
        println(nonce.toBase64().padBase64())
        println(result.toBase64().padBase64())

        val expectedResult = "yfCMSyyGDTt4wRfeHGHFDIzriwgImCHImGYx".decodeBase64()
        assertArrayEquals(expectedResult, result)
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


    @Test
    fun `secretbox with known inputs`() {
        val key = ByteArray(32) { (it and 0xff).toByte() }
        val nonce = ByteArray(24)
        val msg = byteArrayOf(104, 101, 108, 108, 111, 32, 98, 111, 98)
        val expected = arrayOf(153, 105, 253, 5, 217, 101, 111, 62, 75, 165, 94, 199, 110, 190, 255, 4, 34, 107, 156, 45, 247, 138, 221, 92, 62).map { it.toByte() }.toByteArray()

        val ciphertext = nacl.secretbox(msg, nonce, key)
        assertArrayEquals(expected, ciphertext)
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
}