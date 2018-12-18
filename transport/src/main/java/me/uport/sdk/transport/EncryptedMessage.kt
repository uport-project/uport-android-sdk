package me.uport.sdk.transport

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON

/**
 * This class encapsulates an encrypted message that was produced using
 * https://github.com/uport-project/specs/blob/develop/messages/encryption.md
 */
@Serializable
data class EncryptedMessage(
        @SerialName("version")
        val version: String = ASYNC_ENC_ALGORITHM,
        @SerialName("nonce")
        val nonce: String,
        @SerialName("ephemPublicKey")
        val ephemPublicKey: String,
        @SerialName("ciphertext")
        val ciphertext: String
) {
    fun toJson(): String = JSON.stringify(serializer(), this)

    companion object {
        fun fromJson(json: String): EncryptedMessage? = JSON.nonstrict.parse(EncryptedMessage.serializer(), json)

        internal const val ASYNC_ENC_ALGORITHM = "x25519-xsalsa20-poly1305"
    }
}