@file:Suppress("DEPRECATION")

package me.uport.sdk

import android.content.Context
import com.uport.sdk.signer.Signer
import com.uport.sdk.signer.signRawTx
import me.uport.sdk.core.Networks
import me.uport.sdk.endpoints.Sensui
import me.uport.sdk.extensions.waitForTransactionToMine
import me.uport.sdk.identity.Account
import me.uport.sdk.identity.AccountType
import me.uport.sdk.identity.AccountType.Device
import me.uport.sdk.identity.AccountType.IdentityManager
import me.uport.sdk.identity.AccountType.KeyPair
import me.uport.sdk.identity.AccountType.HDKeyPair
import me.uport.sdk.identity.AccountType.MetaIdentityManager
import me.uport.sdk.identity.AccountType.Proxy
import me.uport.sdk.identity.MetaIdentityAccount
import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.signer.MetaIdentitySigner
import me.uport.sdk.signer.TxRelayHelper
import me.uport.sdk.signer.TxRelaySigner
import org.kethereum.functions.encodeRLP
import org.kethereum.model.Address
import org.kethereum.model.Transaction
import org.kethereum.model.createTransactionWithDefaults
import org.walleth.khex.toHexString
import java.math.BigInteger

val DEFAULT_GAS_LIMIT = 3_000_000L.toBigInteger()
val DEFAULT_GAS_PRICE = 20_000_000_000L.toBigInteger()

/**
 * Provides methods for broadcasting transactions that support multiple account types.
 *
 * API volatility: __high__
 */
class Transactions(
        context: Context,
        private val account: Account) {

    private val network = Networks.get(account.network)
    private val progress: ProgressPersistence = ProgressPersistence(context)

    /**
     * A suspending function that takes in a [request] [Transaction] and constructs another [Transaction]
     * with the appropriate `from`, `nonce`, `gasLimit` and `gasPrice`
     * according to the provided [signerType] and the Account object it's applied to.
     *
     * Returns a modified [Transaction] object, ready to be signed and sent.
     */
    private suspend fun buildTransaction(request: Transaction, signerType: AccountType = Proxy): Transaction {
        var from = request.from
        val rpcRelayUrl = network.rpcUrl
        val rpcRelay = JsonRPC(rpcRelayUrl)

        var nonce = BigInteger.ZERO
        when (signerType) {
            Device, KeyPair, HDKeyPair -> {
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
     * Sends an Ethereum transaction.
     * Depending on the type of signer, the process of building, signing and broadcasting a transaction
     * may take a long time. This method can be called multiple times with the same transaction request
     * and it will continue the process.
     */
    @Suppress("ComplexMethod")
    suspend fun sendTransaction(signer: Signer, request: Transaction, signerType: AccountType = Proxy): String {
        val txLabel = request.encodeRLP().toHexString()

        var (state, oldBundle) = if (progress.contains(txLabel)) {
            (ProgressPersistence.PendingTransactionState.NONE to ProgressPersistence.PersistentBundle())
        } else {
            progress.restore(txLabel)
        }

        if (state == ProgressPersistence.PendingTransactionState.NONE) {
            val unsigned = buildTransaction(request, signerType)
            state = ProgressPersistence.PendingTransactionState.TRANSACTION_BUILT
            oldBundle = oldBundle.copy(unsigned = unsigned, ordinal = state.ordinal)
            progress.save(oldBundle, txLabel)
        }

        if (state == ProgressPersistence.PendingTransactionState.TRANSACTION_BUILT) {
            val signedEncodedTx: ByteArray
            val relaySigner = TxRelaySigner(signer, network)
            val txHash = when (signerType) {
                MetaIdentityManager -> {

                    @Suppress("UnsafeCast")
                    val metaAccount = account as MetaIdentityAccount
                    val metaSigner = MetaIdentitySigner(relaySigner, metaAccount.publicAddress, metaAccount.identityManagerAddress)
                    signedEncodedTx = metaSigner.signRawTx(oldBundle.unsigned)

                    relayMetaTransaction(signedEncodedTx)
                }
                KeyPair, HDKeyPair -> {
                    signedEncodedTx = signer.signRawTx(oldBundle.unsigned)
                    relayRawTransaction(signedEncodedTx)
                }
                else -> {

                    signedEncodedTx = relaySigner.signRawTx(oldBundle.unsigned)

                    //relay directly to RPC node
                    relayRawTransaction(signedEncodedTx)
                }
            }
            state = ProgressPersistence.PendingTransactionState.TRANSACTION_SENT
            oldBundle = oldBundle.copy(signed = signedEncodedTx, txHash = txHash, ordinal = state.ordinal)
            progress.save(oldBundle, txLabel)
        }

        if (state == ProgressPersistence.PendingTransactionState.TRANSACTION_SENT) {
            val blockHash = network.waitForTransactionToMine(oldBundle.txHash)
            state = ProgressPersistence.PendingTransactionState.TRANSACTION_CONFIRMED
            oldBundle = oldBundle.copy(blockHash = blockHash, ordinal = state.ordinal)
            progress.save(oldBundle, txLabel)
            return blockHash
        }
        return "no blockhash"
    }

    private suspend fun relayRawTransaction(signedEncodedTx: ByteArray): String {
        return JsonRPC(Networks.get(account.network).rpcUrl)
                .sendRawTransaction(signedEncodedTx.toHexString())
    }

    @Suppress("UnsafeCast")
    private suspend fun relayMetaTransaction(signedEncodedTx: ByteArray): String {
        val metaAccount = account as MetaIdentityAccount
        val network = Networks.get(metaAccount.network)
        val tx = signedEncodedTx.toHexString()
        return Sensui(network.faucetUrl, network.relayUrl)
                .relayMetaTx(tx, network.name, metaAccount.fuelToken)
    }
}