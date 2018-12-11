package me.uport.sdk.transport

import me.uport.sdk.core.padBase64
import me.uport.sdk.core.toBase64
import org.junit.Assert.*
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
    fun `can decrypt EIP1098 message`() {
        val c = Crypto.EncryptedMessage(
                nonce = "1dvWO7uOnBnO7iNDJ9kO9pTasLuKNlej",
                ephemPublicKey = "FBH1/pAEHOOW14Lu3FWkgV3qOEcuL78Zy+qW1RwzMXQ=",
                ciphertext = "f8kBcl/NCyf3sybfbwAKk/np2Bzt9lRVkZejr6uh5FgnNlH/ic62DZzy")
        val decryptedMessage = Crypto.decryptMessage(c, "7e5374ec2ef0d91761a6e72fdf8f6ac665519bfdf6da0a2329cf0d804514b816".hexToByteArray())
        assertEquals("My name is Satoshi Buterin", decryptedMessage)
    }

    @Test
    fun `can deserialize json string to EncryptedMessage`() {
        //language=JSON
        val json = """{"version":"x25519-xsalsa20-poly1305","nonce":"JAX+g+/e3RnnNXHRS4ct5Sb+XdgYoJeY","ephemPublicKey":"JLBIe7eSVyq6egVexeWrlKQyOukSo66G3N0PlimMUyI","ciphertext":"Yr2o6x831YWFZr6KESzSkBqpMv1wYkxPULbVSZi21J+2vywrVeZnDe/U2GW40wzUpLv4HhFgL1kvt+cORrapsqCfSy2L1ltMtkilX06rJ+Q"}"""
        val enc = Crypto.EncryptedMessage.fromJson(json)
        assertNotNull(enc)
        enc!!
        assertEquals("x25519-xsalsa20-poly1305", enc.version)
        assertEquals("JAX+g+/e3RnnNXHRS4ct5Sb+XdgYoJeY", enc.nonce)
        assertEquals("JLBIe7eSVyq6egVexeWrlKQyOukSo66G3N0PlimMUyI", enc.ephemPublicKey)
        assertEquals("Yr2o6x831YWFZr6KESzSkBqpMv1wYkxPULbVSZi21J+2vywrVeZnDe/U2GW40wzUpLv4HhFgL1kvt+cORrapsqCfSy2L1ltMtkilX06rJ+Q", enc.ciphertext)
    }

    @Test
    fun `can serialize EncryptedMessage to json string`() {
        val input = Crypto.EncryptedMessage(
                nonce = "1dvWO7uOnBnO7iNDJ9kO9pTasLuKNlej",
                ephemPublicKey = "FBH1/pAEHOOW14Lu3FWkgV3qOEcuL78Zy+qW1RwzMXQ=",
                ciphertext = "f8kBcl/NCyf3sybfbwAKk/np2Bzt9lRVkZejr6uh5FgnNlH/ic62DZzy")

        //language=JSON
        val expected = """{"version":"x25519-xsalsa20-poly1305","nonce":"1dvWO7uOnBnO7iNDJ9kO9pTasLuKNlej","ephemPublicKey":"FBH1/pAEHOOW14Lu3FWkgV3qOEcuL78Zy+qW1RwzMXQ=","ciphertext":"f8kBcl/NCyf3sybfbwAKk/np2Bzt9lRVkZejr6uh5FgnNlH/ic62DZzy"}"""

        val json = input.toJson()
        assertEquals(expected, json)
    }

}
