package me.uport.sdk.jsonrpc

/**
 * A class that encapsulates the parameters needed for an "eth_call" to a contract
 * The call is made to an [address] with the already encoded [data]
 */
class EthCall(
        private val address: String,
        private val data: String) : JsonRpcBaseRequest(
        "eth_call",
        listOf(
                mapOf("to" to address,
                        "data" to data),
                "latest")
) {

    fun toJsonRpc() : String = toJson()

}
