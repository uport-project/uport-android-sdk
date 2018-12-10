package me.uport.sdk.transport

import me.uport.sdk.core.padBase64
import me.uport.sdk.core.toBase64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.walleth.khex.hexToByteArray


class CryptoTest {

    @Test
    fun `zero padding`() {

        val original = "hello"

        val padded = original.pad()
        assertNotEquals(original, padded)

        val unpadded = padded.unpad()
        assertEquals(original, unpadded)

    }

    @Test
    fun `encrypt and decrypt`() {
        val msg = "Hello EIP1098"
        val bobSecretKey = ByteArray(32) { it.toByte() }
        val bobPublicKeyBase64 = Crypto.getEncryptionPublicKey(bobSecretKey).toBase64().padBase64()
        val enc = Crypto.encryptMessage(msg, bobPublicKeyBase64)
        val recoveredMessage = Crypto.decryptMessage(enc, bobSecretKey)
        assertEquals(msg, recoveredMessage)
    }

    @Test
    fun `decrypt EIP1098 message`() {
        val c = Crypto.EncryptedMessage(
                nonce = "1dvWO7uOnBnO7iNDJ9kO9pTasLuKNlej",
                ephemPublicKey = "FBH1/pAEHOOW14Lu3FWkgV3qOEcuL78Zy+qW1RwzMXQ=",
                ciphertext = "f8kBcl/NCyf3sybfbwAKk/np2Bzt9lRVkZejr6uh5FgnNlH/ic62DZzy")
        val decryptedMessage = Crypto.decryptMessage(c, "7e5374ec2ef0d91761a6e72fdf8f6ac665519bfdf6da0a2329cf0d804514b816".hexToByteArray())
        assertEquals("My name is Satoshi Buterin", decryptedMessage)
    }

}
