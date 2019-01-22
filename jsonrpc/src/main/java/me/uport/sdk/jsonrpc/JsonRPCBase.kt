package me.uport.sdk.jsonrpc

import android.support.annotation.Keep
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.kethereum.extensions.maybeHexToBigInteger
import org.kethereum.extensions.toHexStringNoPrefix
import org.walleth.khex.prepend0xPrefix
import java.math.BigInteger


/**
 * Data class that encapsulates a basic JsonRPC request
 */
@Keep
open class JsonRpcBaseRequest(
        @Json(name = "method")
        val method: String,

        @Json(name = "params")
        val params: Collection<Any>,

        @Json(name = "id")
        val id: Int = 1,

        @Json(name = "jsonrpc")
        val jsonrpc: String = "2.0") {

    /**
     * serializer for this [JsonRpcBaseRequest]
     */
    fun toJson(): String = jsonAdapter.toJson(this) ?: ""

    companion object {
        private val jsonAdapter = moshi.adapter<JsonRpcBaseRequest>(JsonRpcBaseRequest::class.java)
    }
}

/**
 * Data class that encapsulates a generic JsonRPC response
 */
@Keep
open class JsonRpcBaseResponse(
        @Json(name = "result")
        open val result: Any? = null,

        @Json(name = "error")
        val error: JsonRpcError? = null,

        @Json(name = "id")
        val id: Int = 1,

        @Json(name = "jsonrpc")
        val jsonrpc: String = "2.0") {

    companion object {

        /**
         * Deserializer for [JsonRpcBaseResponse]
         */
        fun fromJson(json: String): JsonRpcBaseResponse? = jsonAdapter?.fromJson(json)

        private val jsonAdapter = moshi.adapter<JsonRpcBaseResponse>(JsonRpcBaseResponse::class.java)
    }
}

/**
 * Class that represents an error returned by a JsonRPC endpoint
 */
class JsonRpcError(val code: Int, val message: String) {
    /**
     * Convert this to an [JsonRpcException] so it can be thrown
     */
    fun toException() = JsonRpcException(code, message)
}

/**
 * Exception equivalent of a [JsonRpcError]
 */
class JsonRpcException(
        val code: Int = -32603,
        override val message: String = "Internal error"
) : Exception(message)

/**
 * Data class that encapsulates a log item for eth_getLogs
 */
data class JsonRpcLogItem(
        val address: String,
        val topics: List<String>,
        val data: String,
        val blockNumber: BigInteger,
        val transactionHash: String,
        val transactionIndex: BigInteger,
        val blockHash: String,
        val logIndex: BigInteger,
        val removed: Boolean

)

/**
 * Helper class to convert between hex strings and [BigInteger] in JSON (de)serialization
 */
class JsonRPCSerializers {
    @FromJson
    fun fromJson(hexString: String): BigInteger = hexString.maybeHexToBigInteger()

    @ToJson
    fun toJson(number: BigInteger): String = number.toHexStringNoPrefix().prepend0xPrefix()
}

/**
 * global used to hook into JSON (de)serialization
 */
val moshi: Moshi = Moshi.Builder()
        .add(JsonRPCSerializers())
        .add(KotlinJsonAdapterFactory())
        .build()

