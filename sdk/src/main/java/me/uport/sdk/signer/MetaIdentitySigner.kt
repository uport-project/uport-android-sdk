package me.uport.sdk.signer

import com.uport.sdk.signer.Signer
import me.uport.sdk.MetaIdentityManager
import org.kethereum.extensions.hexToBigInteger
import org.kethereum.model.Address
import org.kethereum.model.SignatureData
import org.kethereum.model.Transaction
import pm.gnosis.model.Solidity
import pm.gnosis.utils.hexToByteArray
import java.math.BigInteger

class MetaIdentitySigner(
        private val wrappedSigner: TxRelaySigner, //this may become more generic
        private val proxyAddress: String,
        private val metaIdentityManagerAddress: String) : Signer {

    /**
     * Signs a buffer by forwarding to the [wrappedSigner]
     */
    override fun signETH(
            rawMessage: ByteArray,
            callback: (err: Exception?, sigData: SignatureData) -> Unit) = wrappedSigner.signETH(rawMessage, callback)

    override fun signJWT(
            rawPayload: ByteArray,
            callback: (err: Exception?, sigData: SignatureData) -> Unit) = wrappedSigner.signJWT(rawPayload, callback)


    /**
     * retrieves the address of the [wrappedSigner]
     */
    override fun getAddress() = wrappedSigner.getAddress()

    /**
     * Takes an [unsignedTx], wraps it as a call to `forwardTo` and signs it using the [wrappedSigner]
     */
    override fun signRawTx(unsignedTx: Transaction, callback: (err: Exception?, signedEncodedTransaction: ByteArray) -> Unit) {

        val finalDestination = unsignedTx.to!!.hex //dont allow unknown destination to reach this far

        val txCopy = unsignedTx.copy(
                value = BigInteger.ZERO,
                to = Address(metaIdentityManagerAddress)
        )

        val newInput = abiEncodeForwardTo(
                wrappedSigner.getAddress(),
                proxyAddress,
                finalDestination,
                unsignedTx.value,
                unsignedTx.input.toByteArray()
        )

        txCopy.input = newInput.hexToByteArray().toList()

        return wrappedSigner.signRawTx(txCopy, callback)
    }

    companion object {

        /**
         * ABI encode an ETH call to `forwardTo` in the MetaIdentityManager contract
         */
        private fun abiEncodeForwardTo(senderAddress: String, proxyAddress: String, finalDestination: String, value: BigInteger, input: ByteArray): String {
            val solSender = Solidity.Address(senderAddress.hexToBigInteger())
            val solProxy = Solidity.Address(proxyAddress.hexToBigInteger())
            val solDestination = Solidity.Address(finalDestination.hexToBigInteger())
            val solValue = Solidity.UInt256(value)
            val solData = Solidity.Bytes(input)
            return MetaIdentityManager.ForwardTo.encode(
                    solSender,
                    solProxy,
                    solDestination,
                    solValue,
                    solData
            )
        }
    }
}