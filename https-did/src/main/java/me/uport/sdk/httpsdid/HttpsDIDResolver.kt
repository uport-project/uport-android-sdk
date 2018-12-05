package me.uport.sdk.httpsdid

import me.uport.sdk.core.urlGet
import me.uport.sdk.universaldid.DIDResolver
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

open class HttpsDIDResolver : DIDResolver {
    override val method: String = "https"

    override suspend fun resolve(did: String): HttpsIdentityDocument = suspendCoroutine { continuation ->
        if (canResolve(did)) {
            val (_, domain) = parseDIDString(did)
            getProfileDocument(domain) { err, ddo ->
                if (err != null) {
                    continuation.resumeWithException(err)
                } else {
                    continuation.resume(HttpsIdentityDocument.fromJson(ddo)!!)
                }
            }
        } else {
            continuation.resumeWithException(java.lang.IllegalArgumentException("The DID('$did') cannot be resolved by the uPort DID resolver"))
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
    open fun getProfileDocument(domain: String, callback: (err: Exception?, ddo: String) -> Unit) {
        val url = "https://$domain/.well-known/did.json"
        urlGet(url, null, callback)//getProfileDocumentSync(domain)
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