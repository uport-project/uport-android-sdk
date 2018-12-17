package me.uport.sdk.ethrdid

import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON
import me.uport.sdk.universaldid.AuthenticationEntry
import me.uport.sdk.universaldid.DIDDocument
import me.uport.sdk.universaldid.PublicKeyEntry
import me.uport.sdk.universaldid.ServiceEntry

/**
 * Classes to describe the DID document corresponding to a particular ethr-did
 */

@Serializable
data class EthrDIDDocument(
        @SerialName("id")
        override val id: String,

        @SerialName("publicKey")
        override val publicKey: List<PublicKeyEntry> = emptyList(),

        @SerialName("authentication")
        override val authentication: List<AuthenticationEntry> = emptyList(),

        @Optional
        @SerialName("service")
        override val service: List<ServiceEntry> = emptyList(),

        @SerialName("@context")
        override val context: String = "https://w3id.org/did/v1"
) : DIDDocument {
    override fun toString(): String = JSON.indented.stringify(EthrDIDDocument.serializer(), this)

    companion object {
        val blank = EthrDIDDocument("")
    }
}


