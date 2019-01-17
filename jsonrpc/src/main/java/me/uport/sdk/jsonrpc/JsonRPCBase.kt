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

    fun toJson(): String = jsonAdapter.toJson(this) ?: ""

    companion object {
        private val jsonAdapter = moshi.adapter<JsonRpcBaseRequest>(JsonRpcBaseRequest::class.java)
    }
}

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

        fun fromJson(json: String): JsonRpcBaseResponse? = jsonAdapter?.fromJson(json)

        private val jsonAdapter = moshi.adapter<JsonRpcBaseResponse>(JsonRpcBaseResponse::class.java)
    }
}

class JsonRpcError(val code: Int, val message: String) {
    fun toException() = JsonRpcException(code, message)
}

class JsonRpcException(
        val code: Int = -32603,
        override val message: String = "Internal error"
) : Exception(message)

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

class JsonRPCSerializers {
    @FromJson
    fun fromJson(hexString: String): BigInteger = hexString.maybeHexToBigInteger()

    @ToJson
    fun toJson(number: BigInteger): String = number.toHexStringNoPrefix().prepend0xPrefix()
}


val moshi: Moshi = Moshi.Builder()
        .add(JsonRPCSerializers())
        .add(KotlinJsonAdapterFactory())
        .build()

