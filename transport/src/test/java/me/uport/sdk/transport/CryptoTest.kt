package me.uport.sdk.transport

import me.uport.knacl.NaClLowLevel
import me.uport.knacl.NaClLowLevel._9
import me.uport.knacl.nacl
import me.uport.sdk.core.decodeBase64
import me.uport.sdk.core.toBase64
import org.junit.Assert
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.walleth.khex.hexToByteArray
import kotlin.math.ceil
import kotlin.math.roundToInt


@ExperimentalUnsignedTypes
class CryptoTest {

    private val ASYNC_ENC_ALGORITHM = "x25519-xsalsa20-poly1305"
    private val BLOCK_SIZE = 64

    fun String.pad(): String {
        val paddedSize = (ceil(length.toDouble() / BLOCK_SIZE) * BLOCK_SIZE).roundToInt()
        return this.padEnd(paddedSize, '\u0000')
    }

    fun String.unpad(): String = this.replace("\u0000+$".toRegex(), "")

    @Test
    fun whatever() {
        val xx = "hello".pad()
        println(xx)
        val hh = xx.unpad()
        println(hh)
    }

    @Test
    fun decrypt() {
        val c = Crypto.EncryptedMessage(
                nonce = "1dvWO7uOnBnO7iNDJ9kO9pTasLuKNlej",
                ephemPublicKey = "FBH1/pAEHOOW14Lu3FWkgV3qOEcuL78Zy+qW1RwzMXQ=",
                ciphertext = "f8kBcl/NCyf3sybfbwAKk/np2Bzt9lRVkZejr6uh5FgnNlH/ic62DZzy")
        val dd = Crypto().decryptMessage(c, "7e5374ec2ef0d91761a6e72fdf8f6ac665519bfdf6da0a2329cf0d804514b816".hexToByteArray())
        println(dd)
    }

    @Test
    fun `from secretKey`() {
        val secret = "2zKGhQCdzrpOoejE+dbIxvN5r+0wCEcUnV465+fXEtc=".decodeBase64()
        val expectedPub = "8hWKNxUltyh4dyBDcg5wKy6i9y0EI+0LeaqG/zuVgXo="
        assertEquals(32, secret.size)
        val (pub, sec) = nacl.boxKeyPairFromSecretKey(secret)
        assertEquals(expectedPub, pub.toBase64())
    }

    @Test
    fun `from scalarmult`() {
        println(NaClLowLevel._9)
        val n = ByteArray(32)
        val result = nacl.scalarMult(n, _9.asByteArray())
        val expected = ubyteArrayOf(47u, 229u, 125u, 163u, 71u, 205u, 98u, 67u, 21u, 40u, 218u, 172u, 95u, 187u, 41u, 7u, 48u, 255u, 246u, 132u, 175u, 196u, 207u, 194u, 237u, 144u, 153u, 95u, 88u, 203u, 59u, 116u)
        assertArrayEquals(expected.asByteArray(), result)
    }

    @Test
    fun `box and boxOpen`() {
        val aliceSecretKey = ByteArray(nacl.crypto_box_SECRETKEYBYTES) { (it and 0xff).toByte() }
        val bobSecretKey = ByteArray(nacl.crypto_box_SECRETKEYBYTES) { ((nacl.crypto_box_SECRETKEYBYTES - it) and 0xff).toByte() }

        val (alicePub, aliceSecret) = nacl.boxKeyPairFromSecretKey(aliceSecretKey)
        val (bobPub, bobSecret) = nacl.boxKeyPairFromSecretKey(bobSecretKey)


        val nonce = ByteArray(nacl.crypto_box_NONCEBYTES)
        val msg = "message to encrypt".toByteArray(Charsets.UTF_8)

        println("bob pub = ${bobPub.toBase64()}")
        println("bob secret = ${bobSecret.toBase64()}")
        println("bob secret = ${bobSecretKey.toBase64()}")
        println("nonce = ${nonce.toBase64()}")
        println("aliceSecret = ${aliceSecret.toBase64()}")

        var a = ""
        (4 downTo 0).forEach { a += " " + it }
        println(a)

        val cipherToBob = nacl.box(msg, nonce, bobPub, aliceSecret)

        println("cipherToBob = ${cipherToBob.toBase64()}")

        val bobReceived = nacl.boxOpen(cipherToBob, nonce, alicePub, bobSecret)
        Assert.assertArrayEquals(msg, bobReceived)
    }
}