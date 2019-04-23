package me.uport.sdk.extensions

import android.content.Context
import kotlinx.coroutines.*
import me.uport.sdk.Transactions
import me.uport.sdk.core.EthNetwork
import me.uport.sdk.core.Networks
import me.uport.sdk.core.UI
import me.uport.sdk.identity.Account
import me.uport.sdk.jsonrpc.JsonRPC
import org.kethereum.extensions.hexToBigInteger
import org.kethereum.extensions.toHexStringNoPrefix
import org.kethereum.model.Address
import org.kethereum.model.createTransactionWithDefaults
import org.walleth.khex.prepend0xPrefix
import java.math.BigInteger

/**
 * fetches the ETH balance of this Account's deviceAddress
 */
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
            from = Address(this.publicAddress)
    )

    return Transactions(context, this)
            .sendTransaction(
                    this.getSigner(context),
                    rawTransaction,
                    this.type)
}

/**
 * Send contract call from account to [contractAddress] with [data] as the ABI encoded function call
 */
suspend fun Account.send(context: Context, contractAddress: String, data: ByteArray): String {
    val rawTransaction = createTransactionWithDefaults(
            input = data.toList(),
            to = Address(contractAddress),
            from = Address(this.publicAddress),
            value = BigInteger.ZERO
    )

    return Transactions(context, this)
            .sendTransaction(
                    this.getSigner(context),
                    rawTransaction,
                    this.type)
}

/**
 * Send [value] amount of WEI ( 1e-18 ETH ) from Account to the address [to]
 */
fun Account.send(context: Context, to: String, value: BigInteger, callback: (err: Exception?, txHash: String) -> Unit) = GlobalScope.launch {
    try {
        val txHash = send(context, to, value)
        withContext(UI) { callback(null, txHash) }
    } catch (ex: Exception) {
        withContext(UI) { callback(ex, "") }
    }
}

/**
 * Send contract call from account to [contractAddress] with [data] as the ABI encoded function call
 */
fun Account.send(context: Context, contractAddress: String, data: ByteArray, callback: (err: Exception?, txHash: String) -> Unit) = GlobalScope.async {
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

private const val POLLING_DELAY_DEFAULT: Long = 5 * 1000L
private const val POLLING_BACKOFF_FACTOR: Double = 1.1
private const val POLLING_DELAY_MAX: Long = 25 * 1000L