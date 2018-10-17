package me.uport.sdk.universaldid

import android.support.annotation.Keep
import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Abstraction for DID documents
 */
interface DIDDocument {
    val context: String?
    val id: String?
    val publicKey: List<PublicKeyEntry>
    val authentication: List<AuthenticationEntry>
    val service: List<ServiceEntry>
}


@Serializable
data class PublicKeyEntry(
        @SerialName("id")
        val id: String,

        @SerialName("type")
        val type: DelegateType,

        @SerialName("owner")
        val owner: String,

        @Optional
        @SerialName("ethereumAddress")
        val ethereumAddress: String? = null,

        @Optional
        @SerialName("publicKeyHex")
        val publicKeyHex: String? = null,

        @Optional
        @SerialName("publicKeyBase64")
        val publicKeyBase64: String? = null,

        @Optional
        @SerialName("publicKeyBase58")
        val publicKeyBase58: String? = null,

        @Optional
        @SerialName("value")
        val value: String? = null
)

@Serializable
data class AuthenticationEntry(
        val type: DelegateType,
        val publicKey: String
)

@Serializable
data class ServiceEntry(
        val type: String,
        val serviceEndpoint: String
)

@Keep
enum class DelegateType {
    Secp256k1VerificationKey2018,
    Secp256k1SignatureAuthentication2018,
    Ed25519VerificationKey2018,
    RsaVerificationKey2018,
    Curve25519EncryptionPublicKey, // encryption key. Usage described here: https://github.com/uport-project/specs/blob/develop/pki/diddocument.md
}