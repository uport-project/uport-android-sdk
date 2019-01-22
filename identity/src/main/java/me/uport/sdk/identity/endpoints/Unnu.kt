package me.uport.sdk.identity.endpoints

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON
import me.uport.sdk.core.HttpClient
import me.uport.sdk.core.Networks
import java.io.IOException

/**
 * Wraps method calls to the meta-identity creation and lookup service
 */
class Unnu(private val httpClient: HttpClient = HttpClient()) {

    companion object {
        private const val UNNU_URL = "https://api.uport.me/unnu"
        private const val identityCreationUrl = "$UNNU_URL/createIdentity"
        private const val identityCheckUrl = "$UNNU_URL/lookup"
    }

    /**
     * Encapsulates the payload data for an "Identity creation request" made to Unnu
     */
    @Serializable
    data class UnnuCreationRequest(
            @SerialName("deviceKey")
            val deviceKey: String,

            @SerialName("recoveryKey")
            val recoveryKey: String,

            @SerialName("blockchain")
            val blockchain: String,

            @SerialName("managerType")
            val managerType: String = "MetaIdentityManager")

    /**
     * Wraps the data needed for a Unnu lookup request
     */
    @Serializable
    data class LookupRequest(val deviceKey: String)

    /**
     * Encapsulates the response data for identity creation or lookup
     */
    @Serializable
    data class IdentityInfo(
            @SerialName("managerType")
            val managerType: String = "MetaIdentityManager",

            @SerialName("managerAddress")
            val managerAddress: String = "",

            @SerialName("txHash")
            val txHash: String? = null,

            @SerialName("identity")
            val proxyAddress: String? = null,

            @SerialName("blockchain")
            val blockchain: String? = null) {

        companion object {
            internal val blank = IdentityInfo()
        }
    }

    /**
     * Wraps the [IdentityInfo] response in an object suitable for receiving JsonRPC responses
     */
    @Serializable
    data class IdentityInfoJRPCResponse(

            @SerialName("status")
            val status: String = "failure",

            @SerialName("message")
            val message: String? = null,

            @SerialName("data")
            val data: IdentityInfo = IdentityInfo()) {

        companion object {
            /**
             * parses a JsonRPC response into an [IdentityInfoJRPCResponse] object
             */
            fun fromJson(json: String): IdentityInfoJRPCResponse = JSON.nonstrict.parse(IdentityInfoJRPCResponse.serializer(), json)
        }
    }

    /**
     * Queries Unnu for the address of the proxy contract created with the [deviceKeyAddress] as owner
     */
    suspend fun lookupIdentityInfo(deviceKeyAddress: String): IdentityInfo {

        val payloadBody = JSON.stringify(LookupRequest.serializer(), LookupRequest(deviceKeyAddress))
        val rawResponse = httpClient.urlPost(identityCheckUrl, payloadBody, null)
        val parsedResponse = IdentityInfoJRPCResponse.fromJson(rawResponse)
        if (parsedResponse.status == "success") {
            return parsedResponse.data
        } else {
            throw IOException("${parsedResponse.message}")
        }
    }

    /**
     * Calls Unnu with the necessary params to deploy an identity proxy contract
     */
    suspend fun requestIdentityCreation(deviceKeyAddress: String,
                                        recoveryAddress: String,
                                        networkId: String,
                                        fuelToken: String): IdentityInfo {

        val unnuPayload = JSON.stringify(UnnuCreationRequest.serializer(),
                UnnuCreationRequest(
                        deviceKeyAddress,
                        recoveryAddress,
                        Networks.get(networkId).name)
        )

        val rawResponse = httpClient.urlPost(identityCreationUrl, unnuPayload, fuelToken)
        val parsedResponse = IdentityInfoJRPCResponse.fromJson(rawResponse)
        if (parsedResponse.status == "success") {
            return parsedResponse.data
        } else {
            throw IOException("${parsedResponse.message}")
        }
    }

}