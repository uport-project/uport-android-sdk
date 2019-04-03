package me.uport.sdk.uportdid

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.uport.mnid.Account
import me.uport.mnid.MNID
import me.uport.sdk.core.Networks
import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.jsonrpc.JsonRpcException
import me.uport.sdk.universaldid.BlankDocumentError
import me.uport.sdk.universaldid.DIDDocument
import me.uport.sdk.universaldid.DIDResolver
import me.uport.sdk.universaldid.DidResolverError
import org.kethereum.encodings.encodeToBase58String
import org.kethereum.extensions.hexToBigInteger
import org.walleth.khex.clean0xPrefix
import org.walleth.khex.hexToByteArray
import pm.gnosis.model.Solidity

/**
 * This is a DID resolver implementation that supports the "uport" DID method.
 * It accepts uport dids or simple mnids and produces a document described at:
 * https://w3c-ccg.github.io/did-spec/#did-documents
 *
 * Example uport did: "did:uport:2nQtiQG6Cgm1GYTBaaKAgr76uY7iSexUkqX#owner"
 * Example mnid: "2nQtiQG6Cgm1GYTBaaKAgr76uY7iSexUkqX"
 */
open class UportDIDResolver(
        private val rpc: JsonRPC
) : DIDResolver {
    override val method: String = "uport"

    override suspend fun resolve(did: String): DIDDocument = withContext(Dispatchers.IO) {
        if (canResolve(did)) {
            val (_, mnid) = parseDIDString(did)
            val ddo = getProfileDocumentFor(mnid)

            ddo?.convertToDIDDocument(did)
                    ?: throw BlankDocumentError("unable to fetch profile document for $did")

        } else {
            throw IllegalArgumentException("The DID('$did') cannot be resolved by the uPort DID resolver")
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

    private fun decodeMnidTargets(issuerId: String?, subjectId: String?): Pair<Account, Account> {
        val issuer = MNID.decode(issuerId ?: subjectId ?: "")

        val subject = MNID.decode(subjectId ?: "")

        if (issuer.network != subject.network) {
            throw(IllegalArgumentException("Issuer and subject must be on the same network"))
        }
        return Pair(issuer, subject)
    }

    private suspend fun getDocAddressFromUportRegistry(
            subjectId: String?,
            issuerId: String? = null,
            registrationIdentifier: String = "uPortProfileIPFS1220"
    ): String {

        val (issuer, subject) = decodeMnidTargets(issuerId, subjectId)

        val network = Networks.get(issuer.network)

        val registryAddress = MNID.decode(network.uPortRegistry).address

        if (registryAddress.isBlank()) {
            throw IllegalStateException("uPort DID registry for [${issuer.network}] was not configured.")
        }

        val encodedFunctionCall = encodeRegistryGetCall(registrationIdentifier, issuer, subject)

        return try {
            rpc.ethCall(registryAddress, encodedFunctionCall)
        } catch (err: JsonRpcException) {
            throw DidResolverError("RPC endpoint returned an error during uPort registry query", err)
        }
    }

    internal fun encodeRegistryGetCall(registrationIdentifier: String, issuer: Account, subject: Account): String {
        val solRegistryIdentifier = Solidity.Bytes32(registrationIdentifier.toByteArray())
        val solIssuer = Solidity.Address(issuer.address.hexToBigInteger())
        val solSubject = Solidity.Address(subject.address.hexToBigInteger())

        return UportRegistry.Get.encode(solRegistryIdentifier, solIssuer, solSubject)
    }

    /**
     * Given an MNID, obtains the IPFS hash of the UportDIDResolver document by eth_call to the uport registry
     */
    internal suspend fun getIpfsHash(mnid: String): String {
        val docAddressHex = getDocAddressFromUportRegistry(mnid)
        return if (docAddressHex.isBlank()) {
            return ""
        } else {
            "1220${docAddressHex.clean0xPrefix()}".hexToByteArray().encodeToBase58String()
        }
    }

    private suspend fun getJsonProfile(mnid: String): String {

        val ipfsHash = getIpfsHash(mnid)

        val url = "https://ipfs.infura.io/ipfs/$ipfsHash"

        return rpc.httpClient.urlGet(url)
    }

    /**
     * Given an [mnid], obtains the JSON encoded DID doc then tries to convert it to a [UportIdentityDocument] object
     *
     * Should return `null` if anything goes wrong
     */
    @Suppress("DEPRECATION")
    internal suspend fun getProfileDocumentFor(mnid: String): UportIdentityDocument? {
        val rawJsonDDO = getJsonProfile(mnid)

        return UportIdentityDocument.fromJson(rawJsonDDO)
    }

    companion object {
        //language=RegExp
        val uportDIDPattern = "^(did:(uport):)?([1-9A-HJ-NP-Za-km-z]{34,38})(.*)".toRegex()

        internal fun parseDIDString(did: String): Pair<String, String> {
            val matchResult = uportDIDPattern.find(did) ?: return ("" to did)
            val (_, method, mnid) = matchResult.destructured
            return (method to mnid)
        }
    }
}
