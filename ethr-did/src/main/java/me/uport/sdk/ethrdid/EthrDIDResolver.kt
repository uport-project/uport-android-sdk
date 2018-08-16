package me.uport.sdk.ethrdid

import android.support.annotation.VisibleForTesting
import android.support.annotation.VisibleForTesting.PRIVATE
import me.uport.sdk.core.*
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

    suspend fun resolve(did: String): DDO {
        val normalizedDid = normalizeDid(did)
        val identity = parseIdentity(normalizedDid)
        val ethrdidContract = EthrDID(identity, rpc, registryAddress, Signer.blank)
        val owner = ethrdidContract.lookupOwner(false)
        val history = getHistory(identity)
        return wrapDidDocument(normalizedDid, owner, history)
    }

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

    fun wrapDidDocument(did: String, owner: String, history: List<Any>): DDO {
        val now = System.currentTimeMillis() / 1000

        val pkEntries = mapOf<String, PublicKeyEntry>().toMutableMap().apply {
            put("owner", PublicKeyEntry(
                    id = "$did#owner",
                    type = Secp256k1VerificationKey2018,
                    owner = did,
                    ethereumAddress = owner
            ))

        }
        val authEntries = mapOf<String, AuthenticationEntry>().toMutableMap().apply {
            put("owner", AuthenticationEntry(
                    type = Secp256k1SignatureAuthentication2018,
                    publicKey = "$did#owner"
            ))
        }
        val serviceEntries = mapOf<String, ServiceEntry>().toMutableMap()

        var delegateCount = 0

        history.forEach { event ->
            when (event) {
                is DIDDelegateChanged.Arguments -> {
                    val delegateType = event.delegatetype.bytes.toString(utf8)
                    val delegate = event.delegate.value.toHexStringNoPrefix().prepend0xPrefix()
                    val key = "DIDDelegateChanged-$delegateType-$delegate"
                    val validTo = event.validto.value.toLong()

                    if (validTo >= now) {
                        delegateCount++

                        when (delegateType) {
                            Secp256k1SignatureAuthentication2018.name,
                            sigAuth -> authEntries[key] = AuthenticationEntry(
                                    type = Secp256k1SignatureAuthentication2018,
                                    publicKey = "$did#delegate-$delegateCount")

                            Secp256k1VerificationKey2018.name,
                            veriKey -> pkEntries[key] = PublicKeyEntry(
                                    id = "$did#delegate-$delegateCount",
                                    type = Secp256k1VerificationKey2018,
                                    owner = did,
                                    ethereumAddress = delegate)
                        }


                    }
                }

                is DIDAttributeChanged.Arguments -> {
                    val validTo = event.validto.value.toLong()
                    if (validTo >= now) {
                        val name = event.name.byteArray.bytes32ToString()
                        val key = "DIDAttributeChanged-$name-${event.value.items.toHexString()}"

                        @Language("RegExp")
                        val regex = """^did/(pub|auth|svc)/(\w+)(/(\w+))?(/(\w+))?$""".toRegex()
                        val matchResult = regex.find(name)
                        if (matchResult != null) {
                            val (section, algo, _, rawType, _, encoding) = matchResult.destructured
                            val type = parseType(algo, rawType)

                            when (section) {

                                "pub" -> {
                                    delegateCount++
                                    val pk = PublicKeyEntry(
                                            id = "$did#delegate-$delegateCount",
                                            type = type,
                                            owner = did)

                                    pkEntries[key] = when (encoding) {
                                        "", "null", "hex" ->
                                            pk.copy(publicKeyHex = event.value.items.toHexString())
                                        "base64" ->
                                            pk.copy(publicKeyBase64 = event.value.items.toBase64())
                                        "base58" ->
                                            pk.copy(publicKeyBase58 = event.value.items.toString(utf8).hexToByteArray().encodeToBase58String())
                                        else ->
                                            pk.copy(value = event.value.items.toHexString())
                                    }

                                }

                                "svc" -> {
                                    serviceEntries[key] = ServiceEntry(
                                            type = algo,
                                            serviceEndpoint = event.value.items.toString(utf8)
                                    )
                                }
                            }
                        }

                    }
                }
            }
        }

        return DDO(
                id = did,
                publicKey = pkEntries.values.toList(),
                authentication = authEntries.values.toList(),
                service = serviceEntries.values.toList()
        )
    }

    companion object {
        const val veriKey = "veriKey"
        const val sigAuth = "sigAuth"

        private val attrTypes = mapOf(
                sigAuth to "SignatureAuthentication2018",
                veriKey to "VerificationKey2018"
        )

        fun parseType(algo: String, rawType: String): DelegateType {
            var type = if (rawType.isBlank()) veriKey else rawType
            type = attrTypes[type] ?: type
            return DelegateType.valueOf("$algo$type") //will throw exception if none found
        }

        //language=RegExp
        private val identityExtractPattern = "^did:ethr:(0x[0-9a-fA-F]{40})".toRegex()

        //language=RegExp
        private val didParsePattern = "^(did:)?((\\w+):)?((0x)([0-9a-fA-F]{40}))".toRegex()

        @VisibleForTesting(otherwise = PRIVATE)
        private fun parseIdentity(normalizedDid: String) = identityExtractPattern
                .find(normalizedDid)
                ?.destructured?.component1() ?: ""

        @VisibleForTesting(otherwise = PRIVATE)
        fun normalizeDid(did: String): String {
            val matchResult = didParsePattern.find(did)
            if (matchResult != null) {
                val (didHeader, _, didType, _, _, hexDigits) = matchResult.destructured
                if (didType.isNotBlank() && didType != "ethr") {
                    //should forward to another resolver
                    return ""
                }
                if (didHeader.isBlank() && didType.isNotBlank()) {
                    //doesn't really look like a did if it only specifies type and not "did:"
                    return ""
                }
                return "did:ethr:0x$hexDigits"
            } else {
                //should forward to another resolver
                return ""
            }
        }

    }
}
