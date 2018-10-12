package me.uport.sdk.universaldid

import android.support.annotation.VisibleForTesting
import android.support.annotation.VisibleForTesting.PRIVATE

object UniversalDID : DIDResolver {

    private val resolvers = mapOf<String, DIDResolver>().toMutableMap()
    fun registerResolver(resolver: DIDResolver) {
        if (resolver.method.isBlank()) {
            return
        }
        resolvers[resolver.method] = resolver
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun clearResolvers() = resolvers.clear()

    override val method: String = ""

    override suspend fun resolve(did: String): DIDDocument {
        val (method, _) = parse(did)
        if (method.isBlank()) return DIDDocument.blank
        return resolvers[method]?.resolve(did) ?: return DIDDocument.blank
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun parse(did: String): Pair<String, String> {
        val matchResult = didPattern.find(did) ?: return ("" to "")
        val (method, identifier) = matchResult.destructured
        return (method to identifier)
    }

    //language=RegExp
    private val didPattern = "^did:(.*?):(.+)".toRegex()
}

interface DIDResolver {
    val method: String
    suspend fun resolve(did: String): DIDDocument

}

interface DIDDocument {

    companion object {
        val blank = object : DIDDocument {}
    }
}