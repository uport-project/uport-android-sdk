package me.uport.sdk.uportdid

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * A class that encapsulates the DID document
 *
 * See [identity_document spec](https://github.com/uport-project/specs/blob/develop/pki/identitydocument.md)
 *
 */
@Deprecated("This class definition is prone to change soon so it's marked Deprecated as opposed to stable")
data class DDO(
        @Json(name = "@context")
        val context: String?, //ex: "http://schema.org"

        @Json(name = "@type")
        val type: String, //ex: "Organization", "Person"

        @Json(name = "publicKey")
        val publicKey: String,  //ex: "0x04613bb3a4874d27032618f020614c21cbe4c4e4781687525f6674089f9bd3d6c7f6eb13569053d31715a3ba32e0b791b97922af6387f087d6b5548c06944ab062"

        @Json(name = "publicEncKey")
        val publicEncKey: String?,  //ex: "0x04613bb3a4874d27032618f020614c21cbe4c4e4781687525f6674089f9bd3d6c7f6eb13569053d31715a3ba32e0b791b97922af6387f087d6b5548c06944ab062"

        @Json(name = "image")
        val image: ProfilePicture?,     //ex: {"@type":"ImageObject","name":"avatar","contentUrl":"/ipfs/QmSCnmXC91Arz2gj934Ce4DeR7d9fULWRepjzGMX6SSazB"}

        @Json(name = "name")
        val name: String?, //ex: "uPort @ Devcon3" , "Vitalik Buterout"

        @Json(name = "description")
        val description: String? // ex: "uPort Attestation"
) {
    companion object {
        fun fromJson(json: String): DDO? {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter<DDO>(DDO::class.java)

            return adapter.fromJson(json)
        }
    }
}

class ProfilePicture(
        @Json(name = "@type")
        val type: String? = "ImageObject",

        @Json(name = "name")
        val name: String? = "avatar",

        @Json(name = "contentUrl")
        val contentUrl: String? = ""
)