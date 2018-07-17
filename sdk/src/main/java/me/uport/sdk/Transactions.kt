package me.uport.sdk

import me.uport.sdk.core.Networks
import me.uport.sdk.core.Signer
import me.uport.sdk.core.signRawTx
import me.uport.sdk.endpoints.Sensui
import me.uport.sdk.extensions.waitForTransactionToMine
import me.uport.sdk.identity.Account
import me.uport.sdk.identity.SignerType
import me.uport.sdk.identity.SignerType.*
import me.uport.sdk.identity.SignerType.MetaIdentityManager
import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.jsonrpc.experimental.getGasPrice
import me.uport.sdk.jsonrpc.experimental.getTransactionCount
import me.uport.sdk.jsonrpc.experimental.sendRawTransaction
import me.uport.sdk.signer.*
import org.kethereum.model.Address
import org.kethereum.model.Transaction
import org.kethereum.model.createTransactionWithDefaults
import org.walleth.khex.toHexString
import java.math.BigInteger

val DEFAULT_GAS_LIMIT = 3_000_000L.toBigInteger()
val DEFAULT_GAS_PRICE = 20_000_000_000L.toBigInteger()

class Transactions(
        private val account: Account
) {

    private val network = Networks.get(account.network)

    /**
     * A suspending function that takes in a [request] [Transaction] and constructs another [Transaction]
     * with the appropriate `from`, `nonce`, `gasLimit` and `gasPrice`
     * according to the provided [signerType] and the [Account] object it's applied to.
     *
     * Returns a modified [Transaction] object, ready to be signed and sent.
     */
    private suspend fun buildTransaction(request: Transaction, signerType: SignerType = Proxy): Transaction {
        var from = request.from
        val rpcRelayUrl = network.rpcUrl
        val rpcRelay = JsonRPC(rpcRelayUrl)

        var nonce = BigInteger.ZERO
        when (signerType) {
            Device, KeyPair -> {
                from = Address(account.deviceAddress)
                nonce = rpcRelay.getTransactionCount(account.deviceAddress)
            }
            MetaIdentityManager -> {
                from = Address(account.publicAddress)
                nonce = TxRelayHelper(network).resolveMetaNonce(account.deviceAddress)
            }
            Proxy, IdentityManager -> {
                from = Address(account.publicAddress)
            }
        }

        val relayPrice = rpcRelay.getGasPrice()
        val gasPrice = when {
            request.gasPrice != BigInteger.ZERO -> request.gasPrice
            relayPrice != BigInteger.ZERO -> relayPrice
            else -> DEFAULT_GAS_PRICE
        }

        val gasLimit = when (request.gasLimit) {
            BigInteger.ZERO -> DEFAULT_GAS_LIMIT
            else -> request.gasLimit
        }

        return createTransactionWithDefaults(
                to = request.to,
                from = from,
                nonce = nonce,
                input = request.input,
                value = request.value,
                gasPrice = gasPrice,
                gasLimit = gasLimit)
    }


    /**
     * sends a given transaction to the Eth network.
     * Depending on the [signerType], the transaction may be wrapped into some other transaction
     *
     */
    suspend fun sendTransaction(signer: Signer, request: Transaction, signerType: SignerType = Proxy): String {
        val unsigned = buildTransaction(request, signerType)

        val relaySigner = TxRelaySigner(signer, network)

        val txHash = when (signerType) {
            MetaIdentityManager -> {

                val metaSigner = MetaIdentitySigner(relaySigner, account.publicAddress, account.identityManagerAddress)
                val signedEncodedTx = metaSigner.signRawTx(unsigned)

                relayMetaTransaction(signedEncodedTx)

            }
            KeyPair -> {
                val signedEncodedTx = signer.signRawTx(unsigned)
                relayRawTransaction(signedEncodedTx)
            }
            else -> {

                val signedEncodedTx = relaySigner.signRawTx(unsigned)

                if (signerType != KeyPair) {
                    //fuel the device key?
                    val refuelTxHash = maybeRefuel(signedEncodedTx)
                    network.waitForTransactionToMine(refuelTxHash)
                }

                //relay directly to RPC node
                relayRawTransaction(signedEncodedTx)
            }
        }
        return txHash
    }

    private suspend fun relayRawTransaction(signedEncodedTx: ByteArray): String {
        return JsonRPC(Networks.get(account.network).rpcUrl)
                .sendRawTransaction(signedEncodedTx.toHexString())
    }

    private suspend fun relayMetaTransaction(signedEncodedTx: ByteArray): String {
        val network = Networks.get(account.network)
        val tx = signedEncodedTx.toHexString()
        return Sensui(network.faucetUrl, network.relayUrl)
                .relayMetaTx(tx, network.name, account.fuelToken)
    }

    private suspend fun maybeRefuel(signedEncodedTx: ByteArray): String {
        val network = Networks.get(account.network)
        val tx = signedEncodedTx.toHexString()
        return Sensui(network.faucetUrl, network.relayUrl)
                .maybeRefuel(tx, network.name, account.fuelToken)
    }

}