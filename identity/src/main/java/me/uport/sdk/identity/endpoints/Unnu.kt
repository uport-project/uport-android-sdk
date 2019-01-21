package me.uport.sdk.identity.endpoints

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON
import me.uport.sdk.core.HttpClient
import me.uport.sdk.core.Networks
import java.io.IOException

class Unnu(private val httpClient: HttpClient = HttpClient()) {

    companion object {
        const val UNNU_URL = "https://api.uport.me/unnu"
        const val identityCreationUrl = "$UNNU_URL/createIdentity"
        const val identityCheckUrl = "$UNNU_URL/lookup"
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
            val managerType: String = "MetaIdentityManager") {

        fun toJson() = JSON.stringify(UnnuCreationRequest.serializer(), this)
    }

    /**
     * Wraps the data needed for a Unnu lookup request
     */
    @Serializable
    data class LookupRequest(val deviceKey: String) {

        fun toJson() = JSON.stringify(LookupRequest.serializer(), this)

    }

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
            fun fromJson(json: String): IdentityInfoJRPCResponse = JSON.nonstrict.parse(IdentityInfoJRPCResponse.serializer(), json)
        }
    }

    /**
     * Queries Unnu for the address of the proxy contract created with the [deviceKeyAddress] as owner
     */
    suspend fun lookupIdentityInfo(deviceKeyAddress: String): IdentityInfo {

        val rawResponse = httpClient.urlPost(identityCheckUrl, LookupRequest(deviceKeyAddress).toJson(), null)
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

        val unnuPayload = UnnuCreationRequest(
                deviceKeyAddress,
                recoveryAddress,
                Networks.get(networkId).name).toJson()

        val rawResponse = httpClient.urlPost(identityCreationUrl, unnuPayload, fuelToken)
        val parsedResponse = IdentityInfoJRPCResponse.fromJson(rawResponse)
        if (parsedResponse.status == "success") {
            return parsedResponse.data
        } else {
            throw IOException("${parsedResponse.message}")
        }
    }

}