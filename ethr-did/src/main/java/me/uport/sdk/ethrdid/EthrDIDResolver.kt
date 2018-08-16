package me.uport.sdk.ethrdid

import me.uport.sdk.core.bytes32ToString
import me.uport.sdk.core.hexToBytes32
import me.uport.sdk.core.toBase64
import me.uport.sdk.core.utf8
import me.uport.sdk.ethrdid.DelegateType.Secp256k1SignatureAuthentication2018
import me.uport.sdk.ethrdid.DelegateType.Secp256k1VerificationKey2018
import me.uport.sdk.ethrdid.EthereumDIDRegistry.Events.DIDAttributeChanged
import me.uport.sdk.ethrdid.EthereumDIDRegistry.Events.DIDDelegateChanged
import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.jsonrpc.JsonRpcBaseResponse
import me.uport.sdk.jsonrpc.experimental.ethCall
import me.uport.sdk.jsonrpc.experimental.getLogs
import org.intellij.lang.annotations.Language
import org.kethereum.encodings.encodeToBase58String
import org.kethereum.extensions.hexToBigInteger
import org.kethereum.extensions.toHexStringNoPrefix
import org.walleth.khex.hexToByteArray
import org.walleth.khex.prepend0xPrefix
import org.walleth.khex.toHexString
import pm.gnosis.model.Solidity
import java.math.BigInteger
import java.util.*

class EthrDIDResolver(
        private val rpc: JsonRPC,
        //TODO: replace hardcoded coordinates with configuration
        val registryAddress: String = "0xdca7ef03e98e0dc2b855be647c39abe984fcf21b"
) {


    suspend fun lastChanged(identity: String): String {
        val encodedCall = EthereumDIDRegistry.Changed.encode(Solidity.Address(identity.hexToBigInteger()))
        val jrpcResponse = rpc.ethCall(registryAddress, encodedCall)
        val parsedResponse = JsonRpcBaseResponse.fromJson(jrpcResponse)

        if (parsedResponse.error != null) throw parsedResponse.error?.toException()!!

        return parsedResponse.result.toString()
    }

    suspend fun getHistory(identity: String): List<Any> {
        val lastChangedQueue: Queue<BigInteger> = PriorityQueue<BigInteger>()
        val events = emptyList<Any>().toMutableList()
        lastChangedQueue.add(lastChanged(identity).hexToBigInteger())
        do {
            val lastChange = lastChangedQueue.remove()
            val logs = rpc.getLogs(registryAddress, listOf(null, identity.hexToBytes32()), lastChange, lastChange)
            logs.forEach {
                val topics: List<String> = it.topics
                val data: String = it.data

                try {
                    val event = EthereumDIDRegistry.Events.DIDOwnerChanged.decode(topics, data)
                    lastChangedQueue.add(event.previouschange.value)
                    events.add(event)
                } catch (err: Exception) { /*nop*/
                }

                try {
                    val event = EthereumDIDRegistry.Events.DIDAttributeChanged.decode(topics, data)
                    lastChangedQueue.add(event.previouschange.value)
                    events.add(event)
                } catch (err: Exception) { /*nop*/
                }

                try {
                    val event = EthereumDIDRegistry.Events.DIDDelegateChanged.decode(topics, data)
                    lastChangedQueue.add(event.previouschange.value)
                    events.add(event)
                } catch (err: Exception) { /*nop*/
                }

            }


        } while (lastChange != null && lastChange != BigInteger.ZERO)

        return events
    }

//    const lastChanged = async (identity) => {
//        const result = await didReg.changed(identity)
//        if (result) {
//            return result['0']
//        }
//    }
//    async function changeLog (identity) {
//        const history = []
//        let previousChange = await lastChanged(identity)
//        while (previousChange) {
//            const logs = await eth.getLogs({address: registryAddress, topics: [null, `0x000000000000000000000000${identity.slice(2)}`], fromBlock: previousChange, toBlock: previousChange})
//            const events = logDecoder(logs)
//            previousChange = undefined
//            for (let event of events) {
//                history.unshift(event)
//                previousChange = event.previousChange
//            }
//        }
//        return history
//    }
//
//    async function resolve (did, parsed) {
//        if (!parsed.id.match(/^0x[0-9a-fA-F]{40}$/)) throw new Error(`Not a valid ethr DID: ${did}`)
//        const owner = await didReg.identityOwner(parsed.id)
//        const history = await changeLog(parsed.id)
//        return wrapDidDocument(did, owner['0'], history)
//    }
}
