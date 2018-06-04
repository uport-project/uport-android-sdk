package me.uport.sdk.extensions

import android.content.Context
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import me.uport.sdk.Transactions
import me.uport.sdk.core.EthNetwork
import me.uport.sdk.core.Networks
import me.uport.sdk.identity.Account
import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.jsonrpc.experimental.getAccountBalance
import me.uport.sdk.jsonrpc.experimental.getTransactionByHash
import org.kethereum.extensions.hexToBigInteger
import org.kethereum.extensions.toHexStringNoPrefix
import org.kethereum.model.Address
import org.kethereum.model.createTransactionWithDefaults
import org.walleth.khex.prepend0xPrefix
import java.math.BigInteger


fun Account.getBalance(callback : (err : Exception?, balance : BigInteger) -> Unit) {
    val network = Networks.get(this.network)
    val rpc = JsonRPC(network.rpcUrl)
    rpc.getAccountBalance(this.deviceAddress, callback)
}

suspend fun Account.getBalance(): BigInteger {
    val network = Networks.get(this.network)
    val rpc = JsonRPC(network.rpcUrl)
    return rpc.getAccountBalance(this.deviceAddress)
}


/**
 * Send [value] amount of WEI ( 1e-18 ETH ) from Account to [destinationAddress]
 */
suspend fun Account.send(context: Context, destinationAddress: String, value: BigInteger): String {
    val rawTransaction = createTransactionWithDefaults(
            value = value,
            to = Address(destinationAddress),
            from = Address(this.proxyAddress)
    )

    return Transactions(this)
            .sendTransaction(
                    this.getSigner(context),
                    rawTransaction,
                    this.signerType)
}

/**
 * Send contract call from account to [contractAddress] with [data] as the ABI encoded function call
 */
suspend fun Account.send(context: Context, contractAddress: String, data: ByteArray): String {
    val rawTransaction = createTransactionWithDefaults(
            input = data.toList(),
            to = Address(contractAddress),
            from = Address(this.proxyAddress),
            value = BigInteger.ZERO
    )

    return Transactions(this)
            .sendTransaction(
                    this.getSigner(context),
                    rawTransaction,
                    this.signerType)
}

/**
 * Send [value] amount of WEI ( 1e-18 ETH ) from Account to the address [to]
 */
fun Account.send(context: Context, to: String, value: BigInteger, callback: (err: Exception?, txHash: String) -> Unit) = async {
    try {
        val txHash = send(context, to, value)
        launch(UI) { callback(null, txHash) }
    } catch (ex: Exception) {
        launch(UI) {
            callback(ex, "")
        }
    }
}

/**
 * Send contract call from account to [contractAddress] with [data] as the ABI encoded function call
 */
fun Account.send(context: Context, contractAddress: String, data: ByteArray, callback: (err: Exception?, txHash: String) -> Unit) = async {
    try {
        val txHash = send(context, contractAddress, data)
        launch(UI) { callback(null, txHash) }
    } catch (ex: Exception) {
        launch(UI) {
            callback(ex, "")
        }
    }
}

suspend fun EthNetwork.waitForTransactionToMine(txHash: String): String {
    //TODO: keep a record of transactions and allow timeouts to happen (measured from when the transaction was first sent)
    val rpc = JsonRPC(this.rpcUrl)

    var minedAtBlockHash = BigInteger.ZERO
    var pollingDelay = POLLING_DELAY_DEFAULT
    while (minedAtBlockHash == BigInteger.ZERO) {
        val transactionInfo = rpc.getTransactionByHash(txHash)
        minedAtBlockHash = transactionInfo.blockHash?.hexToBigInteger()
        pollingDelay = Math.round(pollingDelay * POLLING_BACKOFF_FACTOR)
        pollingDelay = Math.min(pollingDelay, POLLING_DELAY_MAX)
        delay(pollingDelay)
    }
    return minedAtBlockHash.toHexStringNoPrefix().prepend0xPrefix()
}

fun EthNetwork.awaitConfirmation(txHash: String, callback: (err: Exception?, txReceipt: JsonRPC.TransactionReceipt) -> Unit) = launch {

    waitForTransactionToMine(txHash)

    JsonRPC(rpcUrl).getTransactionReceipt(txHash, callback)
}

private const val POLLING_DELAY_DEFAULT: Long = 5 * 1000L
private const val POLLING_BACKOFF_FACTOR: Double = 1.1
private const val POLLING_DELAY_MAX: Long = 25 * 1000L