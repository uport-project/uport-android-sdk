package me.uport.sdk.uportdid

import android.support.annotation.Keep
import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON
import me.uport.sdk.universaldid.AuthenticationEntry
import me.uport.sdk.universaldid.DIDDocument
import me.uport.sdk.universaldid.PublicKeyEntry
import me.uport.sdk.universaldid.PublicKeyType.Companion.Curve25519EncryptionPublicKey
import me.uport.sdk.universaldid.PublicKeyType.Companion.Secp256k1SignatureAuthentication2018
import me.uport.sdk.universaldid.PublicKeyType.Companion.Secp256k1VerificationKey2018
import me.uport.sdk.uportdid.UportDIDResolver.Companion.parseDIDString
import org.walleth.khex.clean0xPrefix

/**
 * A class that encapsulates the legacy uport-did profile document
 *
 * See [identity_document spec](https://github.com/uport-project/specs/blob/develop/pki/identitydocument.md)
 *
 */
@Suppress("DEPRECATION")
@Serializable
@Keep
@Deprecated(message = "this was replaced by UportDIDDocument. use `convertToDIDDocument` to make the transition")
data class UportIdentityDocument(
        @Optional
        @SerialName("@context")
        val context: String? = "http://schema.org",

        @SerialName("@type")
        val type: String, //ex: "Organization", "Person"

        @Optional
        val publicKey: String? = null,  //ex: "0x04613bb3a4874d27032618f020614c21cbe4c4e4781687525f6674089f9bd3d6c7f6eb13569053d31715a3ba32e0b791b97922af6387f087d6b5548c06944ab062"

        @Optional
        val publicEncKey: String? = null,  //ex: "0x04613bb3a4874d27032618f020614c21cbe4c4e4781687525f6674089f9bd3d6c7f6eb13569053d31715a3ba32e0b791b97922af6387f087d6b5548c06944ab062"

        @Optional
        val image: ProfilePicture? = null,     //ex: {"@type":"ImageObject","name":"avatar","contentUrl":"/ipfs/QmSCnmXC91Arz2gj934Ce4DeR7d9fULWRepjzGMX6SSazB"}

        @Optional
        val name: String? = null, //ex: "uPort @ Devcon3" , "Vitalik Buterout"

        @Optional
        val description: String? = null // ex: "uPort Attestation"
) {

    /**
     * Converts the deprecated profile document model to a DID standard compliant [DIDDocument]
     */
    fun convertToDIDDocument(did: String): DIDDocument {

        val normalizedDid = normalizeDID(did)

        val publicVerificationKey = PublicKeyEntry(
                id = "$normalizedDid#keys-1",
                type = Secp256k1VerificationKey2018,
                owner = normalizedDid,
                publicKeyHex = this.publicKey?.clean0xPrefix()
        )
        val authEntries = listOf(AuthenticationEntry(
                type = Secp256k1SignatureAuthentication2018,
                publicKey = "$normalizedDid#keys-1")
        )

        val pkEntries = listOf(publicVerificationKey).toMutableList()

        if (publicEncKey != null) {
            pkEntries.add(PublicKeyEntry(
                    id = "$normalizedDid#keys-2",
                    type = Curve25519EncryptionPublicKey,
                    owner = normalizedDid,
                    publicKeyBase64 = publicEncKey)
            )
        }

        return UportDIDDocument(
                context = "https://w3id.org/did/v1",
                id = normalizedDid,
                publicKey = pkEntries,
                authentication = authEntries,
                uportProfile = copy(
                        context = null,
                        publicEncKey = null,
                        publicKey = null
                ))
    }

    private fun normalizeDID(did: String): String {
        val (_, mnid) = parseDIDString(did)
        return "did:uport:$mnid"
    }

    /**
     * serialize to a json string
     */
    fun toJson(): String = JSON.stringify(UportIdentityDocument.serializer(), this)

    companion object {

        /**
         * Attempts to deserialize a json string into a profile document
         */
        fun fromJson(json: String): UportIdentityDocument? = JSON.nonstrict.parse(UportIdentityDocument.serializer(), json)
    }
}

/**
 * encapsulates a profile picture field of a profile document
 */
@Serializable
class ProfilePicture(
        @Optional
        @SerialName("@type")
        val type: String? = "ImageObject",

        @Optional
        val name: String? = "avatar",

        @Optional
        @Suppress("unused")
        val contentUrl: String? = ""
)

