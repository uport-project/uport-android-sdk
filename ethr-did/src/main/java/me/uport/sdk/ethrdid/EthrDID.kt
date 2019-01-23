package me.uport.sdk.ethrdid

import com.uport.sdk.signer.Signer
import com.uport.sdk.signer.signRawTx
import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.universaldid.DelegateType
import org.kethereum.extensions.hexToBigInteger
import org.kethereum.model.Address
import org.kethereum.model.createTransactionWithDefaults
import org.walleth.khex.hexToByteArray
import org.walleth.khex.prepend0xPrefix
import org.walleth.khex.toHexString
import pm.gnosis.model.Solidity
import java.math.BigInteger

/**
 * Enables interaction with the EthrDID registry contract
 */
class EthrDID(
        private val address: String,
        private val rpc: JsonRPC,
        private val registry: String,
        val signer: Signer
) {

    private val owner: String? = null


    class DelegateOptions(
            val delegateType: DelegateType = DelegateType.Secp256k1VerificationKey2018,
            val expiresIn: Long = 86400L
    )

    suspend fun lookupOwner(cache: Boolean = true): String {
        if (cache && this.owner != null) return this.owner
        val encodedCall = EthereumDIDRegistry.IdentityOwner.encode(Solidity.Address(address.hexToBigInteger()))
        val rawResult = rpc.ethCall(registry, encodedCall)
        return rawResult.substring(rawResult.length - 40).prepend0xPrefix()
    }

    suspend fun changeOwner(newOwner: String): String {
        val owner = lookupOwner()

        val encodedCall = EthereumDIDRegistry.ChangeOwner.encode(
                Solidity.Address(address.hexToBigInteger()),
                Solidity.Address(newOwner.hexToBigInteger())
        )

        return signAndSendContractCall(owner, encodedCall)
    }


    suspend fun addDelegate(delegate: String, options: DelegateOptions = DelegateOptions()): String {
        val owner = lookupOwner()

        val encodedCall = EthereumDIDRegistry.AddDelegate.encode(
                Solidity.Address(this.address.hexToBigInteger()),
                Solidity.Bytes32(options.delegateType.name.toByteArray()),
                Solidity.Address(delegate.hexToBigInteger()),
                Solidity.UInt256(BigInteger.valueOf(options.expiresIn))
        )

        return signAndSendContractCall(owner, encodedCall)
    }

    suspend fun revokeDelegate(delegate: String, delegateType: DelegateType = DelegateType.Secp256k1VerificationKey2018): String {
        val owner = this.lookupOwner()
        val encodedCall = EthereumDIDRegistry.RevokeDelegate.encode(
                Solidity.Address(this.address.hexToBigInteger()),
                Solidity.Bytes32(delegateType.name.toByteArray()),
                Solidity.Address(delegate.hexToBigInteger())
        )

        return signAndSendContractCall(owner, encodedCall)
    }

    suspend fun setAttribute(key: String, value: String, expiresIn: Long = 86400L): String {
        val owner = this.lookupOwner()
        val encodedCall = EthereumDIDRegistry.SetAttribute.encode(
                Solidity.Address(this.address.hexToBigInteger()),
                Solidity.Bytes32(key.toByteArray()),
                Solidity.Bytes(value.toByteArray()),
                Solidity.UInt256(BigInteger.valueOf(expiresIn))
        )
        return signAndSendContractCall(owner, encodedCall)
    }

    private suspend fun signAndSendContractCall(owner: String, encodedCall: String): String {
        //these requests can be done in parallel
        val nonce = rpc.getTransactionCount(owner)
        val networkPrice = rpc.getGasPrice()

        val unsignedTx = createTransactionWithDefaults(
                from = Address(owner),
                to = Address(registry),
                gasLimit = BigInteger.valueOf(70_000),
                //FIXME: allow overriding the gas price
                gasPrice = networkPrice,
                nonce = nonce,
                input = encodedCall.hexToByteArray().toList(),
                value = BigInteger.ZERO
        )

        val signedEncodedTx = signer.signRawTx(unsignedTx)

        return rpc.sendRawTransaction(signedEncodedTx.toHexString())
    }
}