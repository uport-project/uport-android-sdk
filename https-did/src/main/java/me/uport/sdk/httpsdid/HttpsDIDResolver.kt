package me.uport.sdk.httpsdid

import me.uport.sdk.core.HttpClient
import me.uport.sdk.universaldid.BlankDocumentError
import me.uport.sdk.universaldid.DIDResolver
import me.uport.sdk.universaldid.DidResolverError

open class HttpsDIDResolver(private val httpClient: HttpClient = HttpClient()) : DIDResolver {
    override val method: String = "https"

    override suspend fun resolve(did: String): HttpsDIDDocument {
        if (canResolve(did)) {
            val (_, domain) = parseDIDString(did)
            val ddoString = getProfileDocument(domain)
            val ddo = HttpsDIDDocument.fromJson(ddoString)
            return ddo
                    ?: throw BlankDocumentError("no profile document found for `$did`")
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