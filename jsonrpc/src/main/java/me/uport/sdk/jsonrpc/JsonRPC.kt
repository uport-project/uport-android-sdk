@file:Suppress("LiftReturnOrAssignment", "UnnecessaryVariable")

package me.uport.sdk.jsonrpc

import android.support.annotation.VisibleForTesting
import android.support.annotation.VisibleForTesting.PRIVATE
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
open class JsonRPC(private val rpcEndpoint: String, val httpClient: HttpClient = HttpClient()) {

//=============================
// eth_call
//=============================

    /**
     * performs an eth_call
     * the `result` of the JsonRPC call is returned as String.
     * Known parsing errors are caught and rethrown, network errors are bubbled up.
     *
     * See also: https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_call
     *
     * @throws JsonRpcException for error replies coming from the endpoint
     * @throws IOException for network errors or unexpected reply formats
     */
    open suspend fun ethCall(address: String, data: String): String {
        val payloadRequest = JsonRpcBaseRequest(
                method = "eth_call",
                params = listOf(
                        mapOf("to" to address,
                                "data" to data),
                        "latest")
        ).toJson()

        return jsonRpcGenericCall(rpcEndpoint, payloadRequest)
    }


//=============================
// eth_getLogs
//=============================

    /**
     * obtains the list of [JsonRpcLogItem]s corresponding to a given [address] and [topics] between [[fromBlock]..[toBlock]]
     *
     * See also: https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_getlogs
     *
     * @throws JsonRpcException for error replies coming from the endpoint
     * @throws IOException for network errors or unexpected reply formats
     */
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

        val logItemsRaw = jsonRpcGenericCall(rpcEndpoint, payloadRequest)
        val type: ParameterizedType = Types.newParameterizedType(List::class.java, JsonRpcLogItem::class.java)
        val jsonAdapter: JsonAdapter<List<JsonRpcLogItem>> = moshi.adapter(type)
        val logs = jsonAdapter.lenient().fromJson(logItemsRaw) ?: emptyList()
        return logs

    }

//=============================
// eth_gasPrice
//=============================

    /**
     * Obtains the gas price in Wei or throws an error if one occurred
     *
     * See also: https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_gasPrice
     */
    suspend fun getGasPrice(): BigInteger {
        val payloadRequest = JsonRpcBaseRequest(
                method = "eth_gasPrice",
                params = emptyList()
        ).toJson()

        val priceHex = jsonRpcGenericCall(rpcEndpoint, payloadRequest)
        return priceHex.hexToBigInteger()
    }


//=============================
//eth_getTransactionCount
//=============================

    /**
     * Calls back with the number of already mined transactions made from the given address.
     * The number is usable as `nonce` (since nonce is zero indexed)
     *
     * See also: https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_getTransactionCount
     *
     * FIXME: account for pending transactions
     */
    suspend fun getTransactionCount(address: String): BigInteger {
        val payloadRequest = JsonRpcBaseRequest(
                method = "eth_getTransactionCount",
                params = listOf(address, "latest")
        ).toJson()

        val nonceHex = jsonRpcGenericCall(rpcEndpoint, payloadRequest)
        return nonceHex.hexToBigInteger()
    }


//=============================
//eth_getBalance
//=============================

    /**
     * Calls back with the ETH balance of an account (expressed in Wei)
     *
     * See also: https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_getBalance
     */
    suspend fun getAccountBalance(address: String): BigInteger {
        val payloadRequest = JsonRpcBaseRequest(
                method = "eth_getBalance",
                params = listOf(address, "latest")
        ).toJson()

        val weiCountHex = jsonRpcGenericCall(rpcEndpoint, payloadRequest)
        return weiCountHex.hexToBigInteger()
    }


//=============================
// eth_getTransactionReceipt
//=============================

    /**
     * Wrapper for a transaction receipt response
     */
    class JsonRpcReceiptResponse(override val result: TransactionReceipt?) : JsonRpcBaseResponse() {

        companion object {
            private val adapter by lazy {
                moshi.adapter<JsonRpcReceiptResponse>(JsonRpcReceiptResponse::class.java)
            }

            fun fromJson(json: String) = adapter.fromJson(json)
        }
    }

    /**
     * Data representing a Transaction receipt
     */
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

    /**
     * Obtains a transaction receipt for a given [txHash]
     *
     * See also: https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_getTransactionReceipt
     */
    suspend fun getTransactionReceipt(txHash: String): TransactionReceipt {
        val payloadRequest = JsonRpcBaseRequest(
                method = "eth_getTransactionReceipt",
                params = arrayListOf(txHash)
        ).toJson()

        val rawResult = httpClient.urlPost(rpcEndpoint, payloadRequest)
        val parsedResponse = JsonRpcReceiptResponse.fromJson(rawResult)
                ?: throw IOException("RPC endpoint response for transaction receipt query cannot be parsed")
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

    /**
     * wrapper for transaction information responses
     */
    class JsonRpcTxByHashResponse(override val result: TransactionInformation?) : JsonRpcBaseResponse() {

        companion object {
            private val adapter by lazy {
                moshi.adapter<JsonRpcTxByHashResponse>(JsonRpcTxByHashResponse::class.java)
            }

            fun fromJson(json: String) = adapter.fromJson(json)
        }
    }

    /**
     * Obtains the transaction information corresponding to a given [txHash]
     *
     * See also: https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_getTransactionByHash
     */
    suspend fun getTransactionByHash(txHash: String): TransactionInformation {
        val payloadRequest = JsonRpcBaseRequest(
                method = "eth_getTransactionByHash",
                params = arrayListOf(txHash)
        ).toJson()

        val rawResult = httpClient.urlPost(rpcEndpoint, payloadRequest)
        val parsedResponse = JsonRpcTxByHashResponse.fromJson(rawResult)
                ?: throw IOException("RPC endpoint response for transaction information query cannot be parsed")
        if (parsedResponse.error != null) {
            throw parsedResponse.error.toException()
        }

        if (parsedResponse.result != null) {
            return parsedResponse.result
        } else {
            throw TransactionNotFoundException(txHash)
        }
    }

    /**
     * Data representing the transaction information
     */
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

    /**
     * Sends a hex string representing a [signedTransactionHex] to be mined by the ETH network.
     *
     * @return the txHash of the transaction if it is accepted by the JsonRPC node.
     *
     * @throws JsonRpcException for error replies coming from the [rpcEndpoint]
     * @throws IOException for network errors or unexpected reply formats
     */
    suspend fun sendRawTransaction(
            signedTransactionHex: String
    ): String {

        val payloadRequest = JsonRpcBaseRequest(
                method = "eth_sendRawTransaction",
                params = listOf(signedTransactionHex)
        ).toJson()

        return jsonRpcGenericCall(rpcEndpoint, payloadRequest)
    }

    /**
     * Make a base JsonRPCRequest to the [url] with the given [payloadRequest]
     * and attempt to parse the response string into a [JsonRpcBaseResponse]
     * @throws IOException if response is null or if it can't be parsed from JSON
     * @throws JsonRpcException if the response was parsed and an error field was present
     */
    @VisibleForTesting(otherwise = PRIVATE)
    suspend fun jsonRpcGenericCall(url: String, payloadRequest: String): String {
        val rawResult = httpClient.urlPost(url, payloadRequest)
        val parsedResponse = JsonRpcBaseResponse.fromJson(rawResult)
                ?: throw IOException("RPC endpoint response can't be parsed as JSON")
        parsedResponse.error?.let {
            throw it.toException()
        }
        return parsedResponse.result.toString()
    }

}



