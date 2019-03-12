package me.uport.sdk.httpsdid

import me.uport.sdk.core.HttpClient
import me.uport.sdk.universaldid.BlankDocumentError
import me.uport.sdk.universaldid.DIDResolver
import me.uport.sdk.universaldid.DidResolverError

/**
 * This is a DID resolver implementation that supports the "https" DID method.
 * It accepts https-did strings and produces a document described at:
 * https://w3c-ccg.github.io/did-spec/#did-documents
 *
 * Example https did: "did:https:example.com"
 */
open class HttpsDIDResolver(private val httpClient: HttpClient = HttpClient()) : DIDResolver {
    override val method: String = "https"

    override suspend fun resolve(did: String): HttpsDIDDocument {
        if (canResolve(did)) {
            val (_, domain) = parseDIDString(did)
            val ddoString = getProfileDocument(domain)
            if (ddoString.isBlank()) {
                throw BlankDocumentError("no profile document found for `$did`")
            }
            return HttpsDIDDocument.fromJson(ddoString)
        } else {
            throw DidResolverError("The DID('$did') cannot be resolved by the HTTPS DID resolver")
        }
    }

    override fun canResolve(potentialDID: String): Boolean {
        val (method, _) = parseDIDString(potentialDID)
        return (method == this.method)
    }


    private suspend fun getProfileDocument(domain: String): String {
        val url = "https://$domain/.well-known/did.json"
        return httpClient.urlGet(url)
    }

    companion object {
        private val uportDIDPattern = "^(did:(https):)?([-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])".toRegex()

        internal fun parseDIDString(did: String): Pair<String, String> {
            val matchResult = uportDIDPattern.find(did) ?: return ("" to did)
            val (_, method, domain) = matchResult.destructured
            return (method to domain)
        }
    }
}