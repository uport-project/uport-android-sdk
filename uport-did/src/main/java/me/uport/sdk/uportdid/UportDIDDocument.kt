package me.uport.sdk.uportdid

import android.support.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import me.uport.sdk.serialization.moshi
import me.uport.sdk.universaldid.AuthenticationEntry
import me.uport.sdk.universaldid.DIDDocument
import me.uport.sdk.universaldid.PublicKeyEntry
import me.uport.sdk.universaldid.ServiceEntry

/**
 * The [DIDDocument] implementation specific to uport-did resolver.
 * This contains an extra [uportProfile] field that encapsulates the legacy profile document.
 */
@Keep
data class UportDIDDocument(
        override val id: String,
        override val publicKey: List<PublicKeyEntry>,
        override val authentication: List<AuthenticationEntry>,
        override val service: List<ServiceEntry> = emptyList(),

        @Json(name = "@context")
        override val context: String = "https://w3id.org/did/v1",

        val uportProfile: UportIdentityDocument
) : DIDDocument {

    /**
     * Serializes this DID document to a JSON string
     */
    fun toJson(): String = jsonAdapter.toJson(this)

    companion object {

        private val jsonAdapter: JsonAdapter<UportDIDDocument> by lazy {
            moshi.adapter(UportDIDDocument::class.java)
        }

        /**
         * Attempts to deserialize a given [json] string into a [UportDIDDocument]
         */
        fun fromJson(json: String) = jsonAdapter.fromJson(json)
    }
}
