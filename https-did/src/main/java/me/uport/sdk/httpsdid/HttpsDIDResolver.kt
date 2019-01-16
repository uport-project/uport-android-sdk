package me.uport.sdk.httpsdid

import me.uport.sdk.core.experimental.urlGet
import me.uport.sdk.universaldid.DIDResolver

open class HttpsDIDResolver : DIDResolver {
    override val method: String = "https"

    override suspend fun resolve(did: String): HttpsIdentityDocument {
        if (canResolve(did)) {
            val (_, domain) = parseDIDString(did)
            val ddoString = getProfileDocument(domain)
            val ddo = HttpsIdentityDocument.fromJson(ddoString)
            return ddo
                    ?: throw IllegalArgumentException("no profile document found for `$did`")
        } else {
            throw IllegalArgumentException("The DID('$did') cannot be resolved by the HTTPS DID resolver")
        }
    }

    override fun canResolve(potentialDID: String): Boolean {
        val (method, _) = parseDIDString(potentialDID)
        return (method == this.method)
    }


    /**
     * Given a [domain], obtains the JSON encoded DID doc then tries to convert it to a [HttpsIdentityDocument] object
     *
     */
    private suspend fun getProfileDocument(domain: String): String {
        val url = "https://$domain/.well-known/did.json"
        return urlGet(url)
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