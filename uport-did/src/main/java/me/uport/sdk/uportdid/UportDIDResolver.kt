package me.uport.sdk.uportdid

import android.os.Handler
import android.os.Looper
import me.uport.mnid.Account
import me.uport.mnid.MNID
import me.uport.sdk.core.Networks
import me.uport.sdk.core.urlGetSync
import me.uport.sdk.core.urlPostSync
import me.uport.sdk.jsonrpc.EthCall
import me.uport.sdk.jsonrpc.JsonRpcBaseResponse
import me.uport.sdk.universaldid.DIDDocument
import me.uport.sdk.universaldid.DIDResolver
import org.kethereum.encodings.encodeToBase58String
import org.kethereum.extensions.hexToBigInteger
import org.walleth.khex.clean0xPrefix
import org.walleth.khex.hexToByteArray
import pm.gnosis.model.Solidity
import kotlin.coroutines.experimental.suspendCoroutine

/**
 * This is a DID resolver implementation that supports the "uport" DID method.
 * It accepts uport dids or simple mnids and produces a document described at:
 * https://github.com/uport-project/specs/blob/develop/pki/identitydocument.md
 *
 * Example uport did: "did:uport:2nQtiQG6Cgm1GYTBaaKAgr76uY7iSexUkqX#owner"
 * Example mnid: "2nQtiQG6Cgm1GYTBaaKAgr76uY7iSexUkqX"
 */
class UportDIDResolver : DIDResolver {
    override val method: String = "uport"

    override suspend fun resolve(did: String): DIDDocument = suspendCoroutine { continuation ->
        if (canResolve(did)) {
            val (_, mnid) = parseDIDString(did)
            getProfileDocument(mnid) { err, ddo ->
                if (err != null) {
                    continuation.resumeWithException(err)
                } else {
                    continuation.resume(ddo.convertToDIDDocument(did))
                }
            }
        } else {
            continuation.resumeWithException(java.lang.IllegalArgumentException("The DID('$did') cannot be resolved by the uPort DID resolver"))
        }
    }

    override fun canResolve(potentialDID: String): Boolean {
        val (method, mnid) = parseDIDString(potentialDID)
        return if (method == this.method) {
            MNID.isMNID(mnid)
        } else {
            MNID.isMNID(potentialDID)
        }
    }

    /**
     * Given an MNID, calls the uport registry and returns the raw json
     */
    private fun callRegistrySync(subjectId: String?, issuerId: String? = null, registrationIdentifier: String = "uPortProfileIPFS1220"): String {

        val issuer = MNID.decode(issuerId ?: subjectId ?: "")

        val subject = MNID.decode(subjectId ?: "")

        if (issuer.network != subject.network) {
            throw(IllegalArgumentException("Issuer and subject must be on the same network"))
        }

        val network = Networks.get(issuer.network)

        val registryAddress = MNID.decode(network.registry).address

        val encodedFunctionCall = encodeRegistryFunctionCall(registrationIdentifier, issuer, subject)

        val jsonPayload = EthCall(registryAddress, encodedFunctionCall).toJsonRpc()

        //can be async
        val jrpcResponse = urlPostSync(network.rpcUrl, jsonPayload)
        return JsonRpcBaseResponse.fromJson(jrpcResponse).result.toString()
    }

    internal fun encodeRegistryFunctionCall(registrationIdentifier: String, issuer: Account, subject: Account): String {
        val solRegistryIdentifier = Solidity.Bytes32(registrationIdentifier.toByteArray())
        val solIssuer = Solidity.Address(issuer.address.hexToBigInteger())
        val solSubject = Solidity.Address(subject.address.hexToBigInteger())

        return UportRegistry.Get.encode(solRegistryIdentifier, solIssuer, solSubject)
    }

    /**
     * Given an MNID, obtains the IPFS hash of the UportDIDResolver document by eth_call to the uport registry
     */
    internal fun getIpfsHashSync(mnid: String): String {
        val docAddressHex = callRegistrySync(mnid)
        return if (docAddressHex.isBlank()) {
            return ""
        } else {
            "1220${docAddressHex.clean0xPrefix()}".hexToByteArray().encodeToBase58String()
        }
    }

    /**
     * Obtains the JSON encoded UportDIDResolver doc given an mnid
     */
    private fun getJsonProfileSync(mnid: String): String {

        val ipfsHash = getIpfsHashSync(mnid)

        val url = "https://ipfs.infura.io/ipfs/$ipfsHash"

        return urlGetSync(url)
    }

    /**
     * Given an [mnid], obtains the JSON encoded DID doc then tries to convert it to a [UportIdentityDocument] object
     *
     * Should return `null` if anything goes wrong
     */
    internal fun getProfileDocumentSync(mnid: String): UportIdentityDocument? {
        val rawJsonDDO = getJsonProfileSync(mnid)

        return UportIdentityDocument.fromJson(rawJsonDDO)
    }

    /**
     * Given an [mnid], obtains the JSON encoded DID doc then tries to convert it to a [UportIdentityDocument] object
     *
     * TODO: Should [callback] with non-`null` error if anything goes wrong
     */
    fun getProfileDocument(mnid: String, callback: (err: Exception?, ddo: UportIdentityDocument) -> Unit) {

        Thread {
            //safe to call networks
            val ddo = getProfileDocumentSync(mnid)

            //return to UI thread
            Handler(Looper.getMainLooper()).post {
                callback(null, ddo!!)
            }
        }.run()

    }

    companion object {
        //language=RegExp
        private val uportDIDPattern = "^(did:(uport):)?([1-9A-HJ-NP-Za-km-z]{34,38})(.*)".toRegex()

        internal fun parseDIDString(did: String): Pair<String, String> {
            val matchResult = uportDIDPattern.find(did) ?: return ("" to did)
            val (_, method, mnid) = matchResult.destructured
            return (method to mnid)
        }
    }

}
