package me.uport.sdk.signer

import me.uport.sdk.TxRelay
import me.uport.sdk.core.EthNetwork
import me.uport.sdk.core.urlPost
import me.uport.sdk.jsonrpc.EthCall
import me.uport.sdk.jsonrpc.JsonRpcBaseResponse
import org.kethereum.extensions.hexToBigInteger
import org.kethereum.extensions.toBytesPadded
import org.kethereum.model.SignatureData
import pm.gnosis.model.Solidity
import java.math.BigInteger

class TxRelayHelper(private val network: EthNetwork) {

    /**
     * Calls the txRelay contract to get the meta-nonce that corresponds to the provided [deviceAddress]
     */
    suspend fun resolveMetaNonce(deviceAddress: String): BigInteger {
        val solidityDeviceAddress = Solidity.Address(deviceAddress.hexToBigInteger())
        val encodedFunctionCall = TxRelay.GetNonce.encode(solidityDeviceAddress)

        val jsonPayload = EthCall(network.txRelayAddress, encodedFunctionCall).toJsonRpc()

        val jrpcResponse = urlPost(network.rpcUrl, jsonPayload)
        return JsonRpcBaseResponse.fromJson(jrpcResponse).result.toString().hexToBigInteger()
    }

    /**
     * ABI encodes the function and parameters for `relayMetaTx` function call in a TxRelay contract
     */
    fun abiEncodeRelayMetaTx(sig : SignatureData, destination : String, data : ByteArray, whitelistOwner : String = ZERO_ADDRESS) : String {

        val solV = Solidity.UInt8(sig.v.toInt().toBigInteger())
        val solR = Solidity.Bytes32(sig.r.toBytesPadded(32))
        val solS = Solidity.Bytes32(sig.s.toBytesPadded(32))
        val solAddress = Solidity.Address(destination.hexToBigInteger())
        val solData = Solidity.Bytes(data)
        val solWhitelistOwner = Solidity.Address(whitelistOwner.hexToBigInteger())
        return TxRelay.RelayMetaTx.encode(solV, solR, solS, solAddress, solData, solWhitelistOwner)
    }

    companion object {
        const val ZERO_ADDRESS = "0x0000000000000000000000000000000000000000"
    }

}