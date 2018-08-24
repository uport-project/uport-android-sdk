package me.uport.sdk.jsonrpc.experimental

import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.jsonrpc.JsonRpcLogItem
import org.kethereum.model.SignatureData
import org.kethereum.model.Transaction
import java.math.BigInteger
import kotlin.coroutines.experimental.suspendCoroutine

suspend fun JsonRPC.ethCall(address: String, data: String): String = suspendCoroutine { cont ->
    this.ethCall(address, data) { err, jrpcResult ->
        if (err != null) {
            cont.resumeWithException(err)
        } else {
            cont.resume(jrpcResult)
        }
    }
}

suspend fun JsonRPC.getLogs(address: String, topics: List<Any?> = emptyList(), fromBlock: BigInteger, toBlock: BigInteger): List<JsonRpcLogItem> = suspendCoroutine { cont ->
    this.getLogs(address, topics, fromBlock, toBlock) { err, logs ->
        if (err != null) {
            cont.resumeWithException(err)
        } else {
            cont.resume(logs)
        }
    }
}

suspend fun JsonRPC.getGasPrice(): BigInteger = suspendCoroutine { cont ->
    this.getGasPrice { err, price ->
        if (err != null) {
            cont.resumeWithException(err)
        } else {
            cont.resume(price)
        }
    }
}

suspend fun JsonRPC.getTransactionCount(address: String): BigInteger = suspendCoroutine { cont ->
    this.getTransactionCount(address) { err, count ->
        if (err != null) {
            cont.resumeWithException(err)
        } else {
            cont.resume(count)
        }
    }
}

suspend fun JsonRPC.getAccountBalance(address: String): BigInteger = suspendCoroutine { cont ->
    this.getAccountBalance(address) { err, balance ->
        if (err != null) {
            cont.resumeWithException(err)
        } else {
            cont.resume(balance)
        }
    }
}

suspend fun JsonRPC.getTransactionReceipt(txHash: String): JsonRPC.TransactionReceipt? = suspendCoroutine { cont ->
    this.getTransactionReceipt(txHash) { err, receipt ->
        if (err != null) {
            cont.resumeWithException(err)
        } else {
            cont.resume(receipt)
        }
    }
}

suspend fun JsonRPC.getTransactionByHash(txHash: String): JsonRPC.TransactionInformation = suspendCoroutine { cont ->
    this.getTransactionByHash(txHash) { err, info ->
        if (err != null) {
            cont.resumeWithException(err)
        } else {
            cont.resume(info)
        }
    }
}

suspend fun JsonRPC.sendRawTransaction(transaction: Transaction, signature: SignatureData): String = suspendCoroutine { cont ->
    this.sendRawTransaction(transaction, signature) { err, txHash ->
        if (err != null) {
            cont.resumeWithException(err)
        } else {
            cont.resume(txHash)
        }
    }
}

suspend fun JsonRPC.sendRawTransaction(signedTx: String): String = suspendCoroutine { cont ->
    this.sendRawTransaction(signedTx) { err, txHash ->
        if (err != null) {
            cont.resumeWithException(err)
        } else {
            cont.resume(txHash)
        }
    }
}