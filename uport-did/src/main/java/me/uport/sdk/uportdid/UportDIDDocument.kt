package me.uport.sdk.uportdid

import android.support.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.uport.sdk.universaldid.AuthenticationEntry
import me.uport.sdk.universaldid.DIDDocument
import me.uport.sdk.universaldid.PublicKeyEntry
import me.uport.sdk.universaldid.ServiceEntry

/**
 * The [DIDDocument] implementation specific to uport-did resolver.
 * This contains an extra [uportProfile] field that encapsulates the legacy profile document.
 */
@Keep
@Serializable
data class UportDIDDocument(
        override val id: String,
        override val publicKey: List<PublicKeyEntry>,
        override val authentication: List<AuthenticationEntry>,
        override val service: List<ServiceEntry> = emptyList(),

        @SerialName("@context")
        override val context: String = "https://w3id.org/did/v1",

        @Suppress("DEPRECATION")
        val uportProfile: UportIdentityDocument

) : DIDDocument {

    /**
     * Serializes this DID document to a JSON string
     */
    fun toJson(): String = Json.stringify(UportDIDDocument.serializer(), this)

    companion object {

        /**
         * Attempts to deserialize a given [json] string into a [UportDIDDocument]
         */
        fun fromJson(json: String) = Json.nonstrict.parse(UportDIDDocument.serializer(), json)
    }
}
