@file:Suppress("LiftReturnOrAssignment")

package me.uport.sdk.jsonrpc

import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import me.uport.sdk.core.urlPost
import org.kethereum.extensions.hexToBigInteger
import org.kethereum.extensions.toHexStringNoPrefix
import org.kethereum.functions.encodeRLP
import org.kethereum.model.SignatureData
import org.kethereum.model.Transaction
import org.walleth.khex.prepend0xPrefix
import org.walleth.khex.toHexString
import java.lang.reflect.ParameterizedType
import java.math.BigInteger


class JsonRPC(private val rpcUrl: String) {


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

        return urlPost(rpcUrl, payloadRequest, null)
    }


//=============================
// eth_getLogs
//=============================

    suspend fun getLogs(address: String, topics: List<Any?> = emptyList(), fromBlock: BigInteger, toBlock: BigInteger): List<JsonRpcLogItem>{
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

        val rawResult = urlPost(rpcUrl, payloadRequest, null)
        val parsedResponse = JsonRpcBaseResponse.fromJson(rawResult)
        if (parsedResponse.error != null) {
            throw parsedResponse.error.toException()
        }
        val logItemsRaw = parsedResponse.result.toString()
        val type: ParameterizedType = Types.newParameterizedType(List::class.java, JsonRpcLogItem::class.java)
        val jsonAdapter: JsonAdapter<List<JsonRpcLogItem>> = moshi.adapter(type)
        return jsonAdapter.lenient().fromJson(logItemsRaw) ?: emptyList()
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

        val rawResult = urlPost(rpcUrl, payloadRequest)
        val parsedResponse = JsonRpcBaseResponse.fromJson(rawResult)
        if (parsedResponse.error != null) {
            throw parsedResponse.error.toException()
        }
        return parsedResponse.result.toString().hexToBigInteger()
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

        val rawResult = urlPost(rpcUrl, payloadRequest)
        val parsedResponse = JsonRpcBaseResponse.fromJson(rawResult)
        if (parsedResponse.error != null) {
            throw parsedResponse.error.toException()
        }
        return parsedResponse.result.toString().hexToBigInteger()
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

        val rawResult = urlPost(rpcUrl, payloadRequest)
        val parsedResponse = JsonRpcBaseResponse.fromJson(rawResult)
        if (parsedResponse.error != null) {
            throw parsedResponse.error.toException()
        }
        return parsedResponse.result.toString().hexToBigInteger()
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

        val rawResult = urlPost(rpcUrl, payloadRequest)
        val parsedResponse = JsonRpcReceiptResponse.fromJson(rawResult)
        if (parsedResponse?.error != null) {
            throw parsedResponse.error.toException()
        }

        return parsedResponse?.result ?: throw TransactionNotFound(txHash)
    }


//=============================
// eth_getTransactionByHash
//=============================

    class JsonRpcTxByHashResponse(override val result: TransactionInformation) : JsonRpcBaseResponse() {

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

        val rawResult = urlPost(rpcUrl, payloadRequest)
        val parsedResponse = JsonRpcTxByHashResponse.fromJson(rawResult)
        if (parsedResponse?.result != null) {
            return parsedResponse.result
        } else {
            throw TransactionNotFound(txHash)
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

    suspend fun sendRawTransaction(transaction: Transaction, signature: SignatureData): String {
        return sendRawTransaction(transaction.encodeRLP(signature).toHexString())
    }

    suspend fun sendRawTransaction(signedTx: String): String {

        val payloadRequest = JsonRpcBaseRequest(
                method = "eth_sendRawTransaction",
                params = listOf(signedTx)
        ).toJson()

        val rawResult = urlPost(rpcUrl, payloadRequest)
        val parsedResponse = JsonRpcBaseResponse.fromJson(rawResult)
        if (parsedResponse.error != null) {
            throw parsedResponse.error.toException()
        } else {
            return parsedResponse.result.toString()
        }
    }

}

class TransactionNotFound(txHash: String) : RuntimeException("The transaction with hash=$txHash has not been mined yet")



