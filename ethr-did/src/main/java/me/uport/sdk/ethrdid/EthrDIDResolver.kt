package me.uport.sdk.ethrdid

import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.jsonrpc.JsonRpcBaseResponse
import me.uport.sdk.jsonrpc.experimental.ethCall
import org.kethereum.extensions.hexToBigInteger
import pm.gnosis.model.Solidity


class EthrDIDResolver(
        val rpc : JsonRPC
)
{

    //TODO: replace hardcoded coordinates with configuration
    val registryAddress = "0xdca7ef03e98e0dc2b855be647c39abe984fcf21b"


    suspend fun lastChanged(identity : String) : String {
        val encodedCall = EthereumDIDRegistry.Changed.encode(Solidity.Address(identity.hexToBigInteger()))
        val jrpcResponse = rpc.ethCall(registryAddress, encodedCall)
        //TODO: throw jrpc error
        return JsonRpcBaseResponse.fromJson(jrpcResponse).result.toString()
    }

//    suspend fun changeLog(identity : String) {
//        var previousChange = lastChanged(identity)
//        while (previousChange.isNotBlank() && previousChange != "0") {
//            var logs = JsonRPC(rpcUrl).getLogs(registryAddress, topics, fromBlock, toBlock)
//        }
//    }

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
