package me.uport.sdk.universaldid

import android.support.annotation.Keep
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer


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

/**
 * This is a wrapper class for PublicKeyType
 * It is meant to provide a more typesafe way of dealing with these strings.
 * This will be ported to inline classes when that feature of kotlin stabilizes and works properly with serialization
 *
 * see DID Document spec:
 * https://w3c-ccg.github.io/did-spec/#did-documents
 */
@Keep
@Serializable
data class DelegateType(val name: String) {

    @Serializer(forClass = DelegateType::class)
    companion object : KSerializer<DelegateType> {

        override fun serialize(output: Encoder, obj: DelegateType) =
                output.encodeString(obj.name)

        override fun deserialize(input: Decoder): DelegateType = DelegateType(input.decodeString())


        //////////////////////////////
        //some known key types
        //////////////////////////////

        /**
         * Default JWT signing key type used by uPort DIDs
         *
         * see usage in uPort spec: https://github.com/uport-project/specs/blob/develop/pki/diddocument.md
         */
        val Secp256k1VerificationKey2018 = DelegateType("Secp256k1VerificationKey2018")

        /**
         * references a [Secp256k1VerificationKey2018] in a DID document [AuthenticationEntry]
         */
        val Secp256k1SignatureAuthentication2018 = DelegateType("Secp256k1SignatureAuthentication2018")


        /**
         * While not directly generated here, it is treated as [Secp256k1VerificationKey2018]
         */
        val Secp256k1SignatureVerificationKey2018 = DelegateType("Secp256k1SignatureVerificationKey2018")

        /**
         * While not directly generated here, it is treated as [Secp256k1VerificationKey2018]
         */
        val EcdsaPublicKeySecp256k1 = DelegateType("EcdsaPublicKeySecp256k1")

        /**
         * encryption key. Usage described here: https://github.com/uport-project/specs/blob/develop/pki/diddocument.md
         */
        val Curve25519EncryptionPublicKey = DelegateType("Curve25519EncryptionPublicKey")
    }
}