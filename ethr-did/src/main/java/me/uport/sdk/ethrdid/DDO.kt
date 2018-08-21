package me.uport.sdk.ethrdid

import android.support.annotation.Keep
import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON

/**
 * Classes to describe the DID document corresponding to a particular ethr-did
 */

@Serializable
data class DDO(
        @SerialName("id")
        val id: String,

        @SerialName("publicKey")
        val publicKey: List<PublicKeyEntry> = emptyList(),

        @SerialName("authentication")
        val authentication: List<AuthenticationEntry> = emptyList(),

        @Optional
        @SerialName("service")
        val service: List<ServiceEntry> = emptyList(),

        @SerialName("@context")
        val context: String = "https://w3id.org/did/v1"
) {
    override fun toString(): String = JSON.indented.stringify(this)

    companion object {
        val blank = DDO("")
    }
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
        val type : String,
        val serviceEndpoint: String
)

@Keep
enum class DelegateType {
    Secp256k1VerificationKey2018,
    Secp256k1SignatureAuthentication2018,
    Ed25519VerificationKey2018,
    RsaVerificationKey2018,
}

