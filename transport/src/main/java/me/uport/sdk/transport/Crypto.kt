package me.uport.sdk.transport

import me.uport.knacl.nacl
import me.uport.sdk.core.decodeBase64
import me.uport.sdk.core.toBase64
import kotlin.math.ceil
import kotlin.math.roundToInt

class Crypto {

    data class EncryptedMessage(
            val version: String = ASYNC_ENC_ALGORITHM,
            val nonce: String,
            val ephemPublicKey: String,
            val ciphertext: String
    )

    /**
     * Calculates the publicKey usable for encryption corresponding to the given [secretKey]
     */
    fun getEncryptionPublicKey(secretKey: ByteArray): ByteArray {
        val (pk, _) = nacl.boxKeyPairFromSecretKey(secretKey)
        return pk
    }

    /**
     *  Encrypts a message
     *
     *  @param      {String}   message    the message to be encrypted
     *  @param      {String}   boxPub     the public encryption key of the receiver, encoded as base64
     *  @return     {Object}              the encrypted message as an object containing a `version`, `nonce`, `ephemPublicKey` and `ciphertext`
     *  @private
     */
    fun encryptMessage(message: String, boxPub: String): EncryptedMessage {

        val (publicKey, secretKey) = nacl.boxKeyPair()
        val nonce = nacl.randomBytes(nacl.crypto_box_NONCEBYTES)
        val padded = message.pad()
        val ciphertext = nacl.box(padded.toByteArray(Charsets.UTF_8), nonce, boxPub.decodeBase64(), secretKey)
        return EncryptedMessage(
                version = ASYNC_ENC_ALGORITHM,
                nonce = nonce.toBase64(),
                ephemPublicKey = publicKey.toBase64(),
                ciphertext = ciphertext.toBase64())
    }


    /**
     *  Decrypts a message
     *
     *  @param      {Object} encrypted                   The encrypted message object
     *  @param      {String} encrypted.version           The string `x25519-xsalsa20-poly1305`
     *  @param      {String} encrypted.nonce             Base64 encoded nonce
     *  @param      {String} encrypted.ephemPublicKey    Base64 encoded ephemeral public key
     *  @param      {String} encrypted.ciphertext        Base64 encoded ciphertext
     *  @param      {String} secretKey                   The secret key as a Uint8Array
     *  @return     {String}                             The decrypted message
     *  @private
     */

    fun decryptMessage(encrypted: EncryptedMessage, secretKey: ByteArray): String {
        if (encrypted.version != ASYNC_ENC_ALGORITHM) throw IllegalArgumentException("Unsupported encryption algorithm: ${encrypted.version}")
        if (encrypted.ciphertext.isBlank() || encrypted.nonce.isBlank() || encrypted.ephemPublicKey.isBlank()) throw IllegalArgumentException("Invalid encrypted message")
        val decrypted = nacl.boxOpen(
                encrypted.ciphertext.decodeBase64(),
                encrypted.nonce.decodeBase64(),
                encrypted.ephemPublicKey.decodeBase64(),
                secretKey) ?: throw RuntimeException("Could not decrypt message")
        return decrypted.toString(Charsets.UTF_8).unpad()
    }

    companion object {

        private const val ASYNC_ENC_ALGORITHM = "x25519-xsalsa20-poly1305"
        private const val BLOCK_SIZE = 64
        private val matchNullCharAtEnd = "\u0000+$".toRegex()

        fun String.pad(): String {
            val paddedSize = (ceil(length.toDouble() / BLOCK_SIZE) * BLOCK_SIZE).roundToInt()
            return this.padEnd(paddedSize, '\u0000')
        }

        fun String.unpad(): String {
            return this.replace(matchNullCharAtEnd, "")
        }

    }

}