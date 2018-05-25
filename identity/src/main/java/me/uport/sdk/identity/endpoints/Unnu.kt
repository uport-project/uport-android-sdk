package me.uport.sdk.identity.endpoints

import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import me.uport.sdk.core.Networks
import me.uport.sdk.core.urlPost
import java.io.IOException


const val UNNU_URL = "https://api.uport.me/unnu"
const val identityCreationUrl = "$UNNU_URL/createIdentity"
const val identityCheckUrl = "$UNNU_URL/lookup"

typealias IdentityInfoCallback = (err: Exception?, identityInfo: UnnuIdentityInfo) -> Unit

/**
 * Encapsulates the payload data for an "Identity creation request" made to Unnu
 */
data class UnnuCreationRequest(
        @Json(name = "deviceKey")
        val deviceKey: String,

        @Json(name = "recoveryKey")
        val recoveryKey: String,

        @Json(name = "blockchain")
        val blockchain: String,

        @Json(name = "managerType")
        val managerType: String = "MetaIdentityManager") {

    fun toJson() = unnuCreationRequestAdapter?.toJson(this) ?: ""

    companion object {
        /**
         * Adapter used to serialize unnu request object
         */
        private val unnuCreationRequestAdapter = moshi.adapter<UnnuCreationRequest>(UnnuCreationRequest::class.java)
    }
}

/**
 * Wraps the data needed for a Unnu lookup request
 */
data class UnnuLookupRequest(val deviceKey: String) {

    fun toJson() = jsonAdapter?.toJson(this) ?: ""

    companion object {
        private val jsonAdapter: JsonAdapter<UnnuLookupRequest>? = moshi.adapter<UnnuLookupRequest>(UnnuLookupRequest::class.java)
    }
}

/**
 * Encapsulates the response data for identity creation or lookup
 */
data class UnnuIdentityInfo(
        @Json(name = "managerType")
        val managerType: String = "MetaIdentityManager",

        @Json(name = "managerAddress")
        val managerAddress: String = "",

        @Json(name = "txHash")
        val txHash: String? = null,

        @Json(name = "identity")
        val proxyAddress: String? = null,

        @Json(name = "blockchain")
        val blockchain: String? = null) {

    companion object {
        internal val blank = UnnuIdentityInfo()
    }
}

/**
 * Wraps the [UnnuIdentityInfo] response in an object suitable for receiving JsonRPC responses
 */
data class UnnuJRPCResponse(

        @Json(name = "status")
        val status: String = "failure",

        @Json(name = "message")
        val message: String? = null,

        @Json(name = "data")
        val data: UnnuIdentityInfo = UnnuIdentityInfo()) {

    companion object {
        /**
         * Adapter used to de-serialize unnu response object
         */
        private val jsonAdapter = moshi.adapter<UnnuJRPCResponse>(UnnuJRPCResponse::class.java)

        fun fromJson(json: String) = jsonAdapter.fromJson(json) ?: UnnuJRPCResponse()
    }
}

/**
 * Queries Unnu for the address of the proxy contract created with the [deviceKeyAddress] as owner
 */
fun lookupIdentityInfo(deviceKeyAddress: String, callback: IdentityInfoCallback) {

    urlPost(identityCheckUrl, UnnuLookupRequest(deviceKeyAddress).toJson(), null)
    { err, rawResponse ->
        if (err != null) {
            return@urlPost callback(err, UnnuIdentityInfo.blank)
        }
        val parsedResponse = UnnuJRPCResponse.fromJson(rawResponse)
        if (parsedResponse.status == "success") {
            return@urlPost callback(null, parsedResponse.data)
        } else {
            return@urlPost callback(IOException("${parsedResponse.message}"), UnnuIdentityInfo.blank)
        }
    }

}

/**
 * Calls Unnu with the necessary params to deploy an identity proxy contract
 */
fun requestIdentityCreation(deviceKeyAddress: String,
                            recoveryAddress: String,
                            networkId: String,
                            fuelToken: String,
                            callback: IdentityInfoCallback) {

    val unnuPayload = UnnuCreationRequest(
            deviceKeyAddress,
            recoveryAddress,
            Networks.get(networkId).name).toJson()

    urlPost(identityCreationUrl, unnuPayload, fuelToken) { err, rawResponse ->
        if (err != null) {
            return@urlPost callback(err, UnnuIdentityInfo.blank)
        }
        val parsedResponse = UnnuJRPCResponse.fromJson(rawResponse)
        if (parsedResponse.status == "success") {
            return@urlPost callback(null, parsedResponse.data)
        } else {
            return@urlPost callback(IOException("${parsedResponse.message}"), UnnuIdentityInfo.blank)
        }
    }
}





