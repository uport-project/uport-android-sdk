package me.uport.sdk.universaldid

import android.support.annotation.VisibleForTesting
import android.support.annotation.VisibleForTesting.PRIVATE

/**
 * A class to abstract resolving Decentralized Identity (DID) documents
 * from specific implementations based on the [method] component of a DID [String]
 *
 * [DIDResolver] implementations need to be registered using [registerResolver]
 *
 * Known implementations of [DIDResolver] are [ethr-did] and [uport-did]
 */
object UniversalDID : DIDResolver {

    private val resolvers = mapOf<String, DIDResolver>().toMutableMap()

    /**
     * Register a resolver for a particular DID [method]
     */
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

/**
 * Abstraction of various methods of resolving DIDs
 *
 * Each resolver should know the [method] it is supposed to resolve
 * and implement a [resolve] coroutine to eventually return a [DIDDocument] or throw an error
 */
interface DIDResolver {
    val method: String
    suspend fun resolve(did: String): DIDDocument

}

/**
 * Abstraction for DID documents
 */
interface DIDDocument {

    companion object {
        val blank = object : DIDDocument {}
    }
}