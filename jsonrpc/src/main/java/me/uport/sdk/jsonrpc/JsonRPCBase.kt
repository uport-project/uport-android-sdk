package me.uport.sdk.jsonrpc

import android.support.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@Keep
open class JsonRpcBaseRequest(
        @Json(name = "method")
        val method: String,

        @Json(name = "params")
        val params: Collection<Any>,

        @Json(name = "id")
        val id: Int = 1,

        @Json(name="jsonrpc")
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

        fun fromJson(json: String): JsonRpcBaseResponse = jsonAdapter?.fromJson(json)
                ?: JsonRpcBaseResponse()

        private val jsonAdapter = moshi.adapter<JsonRpcBaseResponse>(JsonRpcBaseResponse::class.java)
    }
}

class JsonRpcError(val code: Int, val message: String)

val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()!!

