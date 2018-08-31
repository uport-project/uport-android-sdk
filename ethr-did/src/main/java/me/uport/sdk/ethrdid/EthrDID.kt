package me.uport.sdk.ethrdid

import me.uport.sdk.core.Signer
import me.uport.sdk.ethrdid.DelegateType.Secp256k1VerificationKey2018
import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.jsonrpc.JsonRpcBaseResponse
import org.kethereum.extensions.hexToBigInteger
import org.kethereum.model.Address
import org.kethereum.model.createTransactionWithDefaults
import org.walleth.khex.hexToByteArray
import org.walleth.khex.prepend0xPrefix
import org.walleth.khex.toHexString
import pm.gnosis.model.Solidity
import java.math.BigInteger

class EthrDID(
        private val address: String,
        private val rpc: JsonRPC,
        private val registry: String,
        var signer: Signer
) {

    private val owner: String? = null


    class DelegateOptions(
            val delegateType: DelegateType = Secp256k1VerificationKey2018,
            val expiresIn: Long = 86400L
    )


    suspend fun lookupOwner(cache: Boolean = true): String {
        if (cache && this.owner != null) return this.owner
        val encodedCall = EthereumDIDRegistry.IdentityOwner.encode(Solidity.Address(address.hexToBigInteger()))
        val jrpcResponse = rpc.ethCall(registry, encodedCall)
        val rawResult = JsonRpcBaseResponse.fromJson(jrpcResponse).result.toString()
        return rawResult.substring(rawResult.length - 40).prepend0xPrefix()
    }

    suspend fun changeOwner(newOwner: String): String {
        val owner = lookupOwner()

        val encodedCall = EthereumDIDRegistry.ChangeOwner.encode(
                Solidity.Address(address.hexToBigInteger()),
                Solidity.Address(newOwner.hexToBigInteger())
        )

        val txHash = signAndSendContractCall(owner, encodedCall)

        return txHash
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

    suspend fun revokeDelegate(delegate: String, delegateType: DelegateType = Secp256k1VerificationKey2018): String {
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
//
//    // Create a temporary signing delegate able to sign JWT on behalf of identity
//    suspend fun createSigningDelegate(delegateType: String = "Secp256k1VerificationKey2018", expiresIn: Long = 86400L) {
//        val kp = createKeyPair()
//        this.signer = SimpleSigner(kp.privateKey)
//        const txHash = await this.addDelegate(kp.address, { delegateType, expiresIn })
//        return { kp, txHash }
//    }
//
//    async signJWT (payload, expiresIn)
//    {
//        if (typeof this.signer !== 'function') throw new Error('No signer configured')
//        const options = { signer: this.signer, alg: 'ES256K-R', issuer: this.did }
//        if (expiresIn) options.expiresIn = expiresIn
//        return createJWT(payload, options)
//    }
//
//    async verifyJWT (jwt, audience=this.did)
//    {
//        return verifyJWT(jwt, { audience })
//    }

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

        val txHash = rpc.sendRawTransaction(signedEncodedTx.toHexString())
        return txHash
    }
}