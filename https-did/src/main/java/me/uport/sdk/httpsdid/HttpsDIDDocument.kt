package me.uport.sdk.httpsdid

import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import kotlinx.serialization.Optional
import me.uport.sdk.serialization.moshi
import me.uport.sdk.universaldid.AuthenticationEntry
import me.uport.sdk.universaldid.DIDDocument
import me.uport.sdk.universaldid.PublicKeyEntry
import me.uport.sdk.universaldid.ServiceEntry

/**
 * Encapsulates the fields of a Decentralized Identity Document
 */
data class HttpsDIDDocument(
        @Json(name = "@context")
        override val context: String = "https://w3id.org/did/v1",

        @Json(name = "id")
        override val id: String, //ex: "did:https:example.com#owner"

        @Json(name = "publicKey")
        override val publicKey: List<PublicKeyEntry> = emptyList(),

        @Json(name = "authentication")
        override val authentication: List<AuthenticationEntry> = emptyList(),

        @Optional
        @Json(name = "service")
        override val service: List<ServiceEntry> = emptyList()

) : DIDDocument {

    /**
     * Serializes this [HttpsDIDDocument] into a JSON string
     */
    fun toJson(): String = jsonAdapter.toJson(this)

    companion object {
        private val jsonAdapter: JsonAdapter<HttpsDIDDocument> by lazy {
            moshi.adapter(HttpsDIDDocument::class.java)
        }

        /**
         * Attempts to deserialize a given [json] string into a [HttpsDIDDocument]
         */
        fun fromJson(json: String): HttpsDIDDocument? = jsonAdapter.fromJson(json)
    }

}