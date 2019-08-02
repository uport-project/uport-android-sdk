@file:Suppress("DEPRECATION")

package me.uport.sdk.signer

import me.uport.sdk.DEFAULT_GAS_LIMIT
import me.uport.sdk.DEFAULT_GAS_PRICE
import me.uport.sdk.core.EthNetwork
import me.uport.sdk.signer.TxRelayHelper.Companion.ZERO_ADDRESS
import org.kethereum.extensions.toBytesPadded
import org.kethereum.functions.encodeRLP
import org.kethereum.model.Address
import org.kethereum.model.SignatureData
import org.kethereum.model.Transaction
import org.kethereum.model.createTransactionWithDefaults
import org.walleth.khex.clean0xPrefix
import org.walleth.khex.hexToByteArray
import org.walleth.khex.toNoPrefixHexString
import java.math.BigInteger

/**
 * A [Signer] used to wrap transactions as calls to `relayMetaTx` function in a uport TxRelay contract
 */
@Deprecated("uPort meta transactions are no longer supported")
class TxRelaySigner(private val wrappedSigner: Signer,
                    private val network: EthNetwork) : Signer {

    /**
     * forwards the address of the [wrappedSigner]
     */
    override fun getAddress(): String = wrappedSigner.getAddress()

    /**
     * signs a buffer using the [wrappedSigner]
     */
    override fun signETH(rawMessage: ByteArray, callback: (err: Exception?, sigData: SignatureData) -> Unit) = wrappedSigner.signETH(rawMessage, callback)

    override fun signJWT(
            rawPayload: ByteArray,
            callback: (err: Exception?, sigData: SignatureData) -> Unit) = wrappedSigner.signJWT(rawPayload, callback)

    /**
     * Takes in an [unsignedTx], wraps it as a call to `relayMetaTx` and signs it using the [wrappedSigner]
     *
     * Calls back with the RLP encoded signed transaction
     */
    override fun signRawTx(unsignedTx: Transaction, callback: (err: Exception?, signedEncodedTx: ByteArray) -> Unit) {

        val nonce = unsignedTx.nonce ?: 0.toBigInteger()
        val to = unsignedTx.to ?: Address(ZERO_ADDRESS)
        val data = unsignedTx.input

        val sender = wrappedSigner.getAddress() // device address

        // Tight packing for keccak
        val hashInput = "0x1900" +
                network.txRelayAddress.clean0xPrefix() +
                whitelistOwner.clean0xPrefix() +
                nonce.toBytesPadded(32).toNoPrefixHexString() +
                to.cleanHex +
                data.toNoPrefixHexString()

        signETH(hashInput.hexToByteArray()) { err, signature ->

            if (err != null) {
                return@signETH callback(err, byteArrayOf())
            }

            val rawMetaTxData = TxRelayHelper(network)
                    .abiEncodeRelayMetaTx(signature, to.hex, data, whitelistOwner)
                    .hexToByteArray()

            val wrapperTx = createTransactionWithDefaults(
                    gasPrice = unsignedTx.gasPrice ?: DEFAULT_GAS_PRICE,
                    gasLimit = unsignedTx.gasLimit ?: DEFAULT_GAS_LIMIT,
                    value = BigInteger.ZERO,
                    to = Address(network.txRelayAddress),
                    nonce = nonce,
                    from = Address(sender),
                    input = rawMetaTxData
            )

            return@signETH callback(null, wrapperTx.encodeRLP())
        }


    }

    companion object {
        //for the moment, the whitelist owner is all zeroes
        private const val whitelistOwner: String = ZERO_ADDRESS
    }


}