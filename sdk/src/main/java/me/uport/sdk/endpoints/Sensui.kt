package me.uport.sdk.endpoints

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import me.uport.sdk.core.urlPost

class Sensui(
        private val fundUrl: String = SENSUI_DEFAULT_FUND_URL,
        private val relayUrl: String = SENSUI_DEFAULT_RELAY_URL) {

    suspend fun maybeRefuel(signedEncodedTx: String, blockchainName: String, fuelToken: String): String {
        val jsonPayload = SensuiFundRequest(signedEncodedTx, blockchainName).toJson()

        //TODO: saner error checking
        val rawResponse = urlPost(fundUrl, jsonPayload, fuelToken)
        return SensuiResponse.fromJson(rawResponse)?.data ?: ""
    }

    suspend fun relayMetaTx(tx: String, name: String, fuelToken: String): String {
        val jsonPayload = SensuiRelayRequest(tx, name).toJson()

        //TODO: saner error checking
        val rawResponse = urlPost(relayUrl, jsonPayload, fuelToken)
        return SensuiResponse.fromJson(rawResponse)?.data ?: ""
    }

    /**
     * Encapsulates a sensui refuel request payload
     */
    class SensuiFundRequest(
            @Json(name = "tx")
            val signedEncodedTx: String,

            @Json(name = "blockchain")
            val blockchainName: String) {
        fun toJson() = moshi.adapter<SensuiFundRequest>(SensuiFundRequest::class.java)?.toJson(this)
                ?: ""
    }

    /**
     * Encapsulates a sensui metaTx relay request payload
     */
    open class SensuiRelayRequest(
            @Json(name = "metaSignedTx")
            open val signedMetaTx: String,

            @Json(name = "blockchain")
            val blockchainName: String) {
        fun toJson() = moshi.adapter<SensuiRelayRequest>(SensuiRelayRequest::class.java)?.toJson(this)
                ?: ""
    }

    /**
     * Encapsulates a nisaba response payload
     */
    class SensuiResponse(
            @Json(name = "status")
            val status : String,

            @Json(name = "data")
            val data : String,

            @Json(name = "error")
            val error : Any?
    ) {
        companion object {
            fun fromJson(json: String): SensuiResponse? =
                    moshi.adapter<SensuiResponse>(SensuiResponse::class.java).fromJson(json)
        }
    }


    companion object {

        private val moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
        private const val SENSUI_DEFAULT_ROOT_URL = "https://api.uport.me/sensui"
        const val SENSUI_DEFAULT_FUND_URL = "$SENSUI_DEFAULT_ROOT_URL/fund"
        const val SENSUI_DEFAULT_RELAY_URL = "$SENSUI_DEFAULT_ROOT_URL/relay"

    }
}