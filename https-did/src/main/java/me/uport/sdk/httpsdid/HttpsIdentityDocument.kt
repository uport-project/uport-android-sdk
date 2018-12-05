package me.uport.sdk.httpsdid

import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import kotlinx.serialization.Optional
import me.uport.sdk.serialization.moshi
import me.uport.sdk.universaldid.*

data class HttpsIdentityDocument(
        @Json(name="@context")
        override val context: String = "https://w3id.org/did/v1",

        @Json(name="id")
        override val id: String, //ex: "did:https:example.com#owner"

        @Json(name="publicKey")
        override val publicKey: List<PublicKeyEntry> = emptyList(),

        @Json(name="authentication")
        override val authentication: List<AuthenticationEntry> = emptyList(),

        @Optional
        @Json(name="service")
        override val service: List<ServiceEntry> = emptyList()

) : DIDDocument {

    fun toJson(): String = jsonAdapter.toJson(this)

    companion object {
        private val jsonAdapter: JsonAdapter<HttpsIdentityDocument> by lazy {
            moshi.adapter(HttpsIdentityDocument::class.java)
        }

        fun fromJson(json: String): HttpsIdentityDocument? = jsonAdapter.fromJson(json)

        val blank = HttpsIdentityDocument(context="", id="")
    }

}