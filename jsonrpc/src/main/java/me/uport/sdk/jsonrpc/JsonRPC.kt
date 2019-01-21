@file:Suppress("LiftReturnOrAssignment")

package me.uport.sdk.jsonrpc

import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import me.uport.sdk.core.HttpClient
import org.kethereum.extensions.hexToBigInteger
import org.kethereum.extensions.toHexStringNoPrefix
import org.walleth.khex.prepend0xPrefix
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.math.BigInteger

/**
 * Partial wrapper for JsonRPC methods supported by ethereum nodes.
 */
class JsonRPC(private val rpcEndpoint: String, val httpClient: HttpClient = HttpClient()) {

//=============================
// eth_call
//=============================

    /**
     * performs an eth_call
     * the result is returned as raw string and has to be parsed into a Json that can make sense of the expected result
     */
    suspend fun ethCall(address: String, data: String): String {
        val payloadRequest = JsonRpcBaseRequest(
                method = "eth_call",
                params = listOf(
                        mapOf("to" to address,
                                "data" to data),
                        "latest")
        ).toJson()

        return httpClient.urlPost(rpcEndpoint, payloadRequest, null)
    }


//=============================
// eth_getLogs
//=============================

    suspend fun getLogs(address: String, topics: List<Any?> = emptyList(), fromBlock: BigInteger, toBlock: BigInteger): List<JsonRpcLogItem> {
        val payloadRequest = JsonRpcBaseRequest(
                method = "eth_getLogs",
                params = listOf(
                        mapOf(
                                "fromBlock" to fromBlock.toHexStringNoPrefix().prepend0xPrefix(),
                                "toBlock" to toBlock.toHexStringNoPrefix().prepend0xPrefix(),
                                "address" to address,
                                "topics" to topics
                        )
                )
        ).toJson()

        val rawResult = httpClient.urlPost(rpcEndpoint, payloadRequest, null)
        val parsedResponse = JsonRpcBaseResponse.fromJson(rawResult)
                ?: throw IOException("RPC endpoint response can't be parsed as JSON")
        if (parsedResponse.error != null) {
            throw parsedResponse.error.toException()
        }
        val logItemsRaw = parsedResponse.result.toString()
        val type: ParameterizedType = Types.newParameterizedType(List::class.java, JsonRpcLogItem::class.java)
        val jsonAdapter: JsonAdapter<List<JsonRpcLogItem>> = moshi.adapter(type)
        val logs = jsonAdapter.lenient().fromJson(logItemsRaw) ?: emptyList()
        return logs


    }

//=============================
// eth_gasPrice
//=============================

    /**
     * calls back with the gas price in Wei or an error if one occurred
     */
    suspend fun getGasPrice(): BigInteger {
        val payloadRequest = JsonRpcBaseRequest(
                method = "eth_gasPrice",
                params = emptyList()
        ).toJson()

        val parsedResponse = jsonRpcBaseCall(rpcEndpoint, payloadRequest)
        val priceInWei = parsedResponse.result.toString().hexToBigInteger()
        return priceInWei
    }


//=============================
//eth_getTransactionCount
//=============================

    /**
     * Calls back with the number of transactions made from the given address.
     * The number is usable as `nonce` (since nonce is zero indexed)
     *
     * FIXME: account for pending transactions
     */
    suspend fun getTransactionCount(address: String): BigInteger {
        val payloadRequest = JsonRpcBaseRequest(
                method = "eth_getTransactionCount",
                params = listOf(address, "latest")
        ).toJson()

        val parsedResponse = jsonRpcBaseCall(rpcEndpoint, payloadRequest)
        val count = parsedResponse.result.toString().hexToBigInteger()
        return count
    }


//=============================
//eth_getBalance
//=============================

    /**
     * Calls back with the number of transactions made from the given address.
     * The number is usable as `nonce` (since nonce is zero indexed)
     */
    suspend fun getAccountBalance(address: String): BigInteger {
        val payloadRequest = JsonRpcBaseRequest(
                method = "eth_getBalance",
                params = listOf(address, "latest")
        ).toJson()

        val parsedResponse = jsonRpcBaseCall(rpcEndpoint, payloadRequest)
        val balanceInWei = parsedResponse.result.toString().hexToBigInteger()
        return balanceInWei
    }


//=============================
// eth_getTransactionReceipt
//=============================

    class JsonRpcReceiptResponse(override val result: TransactionReceipt?) : JsonRpcBaseResponse() {

        companion object {
            private val adapter by lazy {
                moshi.adapter<JsonRpcReceiptResponse>(JsonRpcReceiptResponse::class.java)
            }

            fun fromJson(json: String) = adapter.fromJson(json)
        }
    }

    data class TransactionReceipt(

            @Json(name = "transactionHash")
            val transactionHash: String? = "",

            @Json(name = "transactionIndex")
            val transactionIndex: String? = "",

            @Json(name = "blockNumber")
            val blockNumber: String? = "",

            @Json(name = "blockHash")
            val blockHash: String? = "",

            @Json(name = "cumulativeGasUsed")
            val cumulativeGasUsed: String? = "",

            @Json(name = "contractAddress")
            val contractAddress: String? = null,

            @Json(name = "logs")
            val logs: List<JsonRpcLogItem?>? = null,

            @Json(name = "logsBloom")
            val logsBloom: String? = "",

            @Json(name = "status")
            val status: String? = "0x0"
    )

    suspend fun getTransactionReceipt(txHash: String): TransactionReceipt {
        val payloadRequest = JsonRpcBaseRequest(
                method = "eth_getTransactionReceipt",
                params = arrayListOf(txHash)
        ).toJson()

        val rawResult = httpClient.urlPost(rpcEndpoint, payloadRequest)
        val parsedResponse = JsonRpcReceiptResponse.fromJson(rawResult)
                ?: throw IOException("RPC endpoint response can't be parsed as JSON")
        if (parsedResponse.error != null) {
            throw parsedResponse.error.toException()
        }

        if (parsedResponse.result != null) {
            return parsedResponse.result
        } else {
            throw TransactionNotFoundException(txHash)
        }
    }


//=============================
// eth_getTransactionByHash
//=============================

    class JsonRpcTxByHashResponse(override val result: TransactionInformation?) : JsonRpcBaseResponse() {

        companion object {
            private val adapter by lazy {
                moshi.adapter<JsonRpcTxByHashResponse>(JsonRpcTxByHashResponse::class.java)
            }

            fun fromJson(json: String) = adapter.fromJson(json)
        }
    }

    suspend fun getTransactionByHash(txHash: String): TransactionInformation {
        val payloadRequest = JsonRpcBaseRequest(
                method = "eth_getTransactionByHash",
                params = arrayListOf(txHash)
        ).toJson()

        val rawResult = httpClient.urlPost(rpcEndpoint, payloadRequest)
        val parsedResponse = JsonRpcTxByHashResponse.fromJson(rawResult)
                ?: throw IOException("RPC endpoint response can't be parsed as JSON")
        if (parsedResponse.error != null) {
            throw parsedResponse.error.toException()
        }

        if (parsedResponse.result != null) {
            return parsedResponse.result
        } else {
            throw TransactionNotFoundException(txHash)
        }
    }

    data class TransactionInformation(
            @Json(name = "hash")
            val txHash: String? = null,

            @Json(name = "nonce")
            val nonce: String? = null,

            @Json(name = "blockHash")
            val blockHash: String? = null,

            @Json(name = "blockNumber")
            val blockNumber: String? = null,

            @Json(name = "transactionIndex")
            val transactionIndex: String? = null,

            @Json(name = "from")
            val from: String = "",

            @Json(name = "to")
            val to: String = "",

            @Json(name = "value")
            val value: String = "",

            @Json(name = "gas")
            val gas: String = "",

            @Json(name = "gasPrice")
            val gasPrice: String = "",

            @Json(name = "input")
            val input: String = ""
    )


//=============================
//eth_sendRawTransaction
//=============================

    suspend fun sendRawTransaction(
            signedTx: String
    ): String {

        val payloadRequest = JsonRpcBaseRequest(
                method = "eth_sendRawTransaction",
                params = listOf(signedTx)
        ).toJson()

        val parsedResponse = jsonRpcBaseCall(rpcEndpoint, payloadRequest)
        return parsedResponse.result.toString()
    }

    /**
     * Make a base JsonRPCRequest to the [url] with the given [payloadRequest]
     * and attempt to parse the response string into a [JsonRpcBaseResponse]
     * @throws IOException if response is null or if it can't be parsed from JSON
     * @throws JsonRpcException if the response was parsed and an error field was present
     */
    private suspend fun jsonRpcBaseCall(url: String, payloadRequest: String): JsonRpcBaseResponse {
        val rawResult = httpClient.urlPost(url, payloadRequest)
        val parsedResponse = JsonRpcBaseResponse.fromJson(rawResult)
                ?: throw IOException("RPC endpoint response can't be parsed as JSON")
        parsedResponse.error?.let {
            throw it.toException()
        }
        return parsedResponse
    }

}

class TransactionNotFoundException(txHash: String) : RuntimeException("The transaction with hash=$txHash has not been mined yet")



