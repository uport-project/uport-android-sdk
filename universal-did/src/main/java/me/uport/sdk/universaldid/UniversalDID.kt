package me.uport.sdk.universaldid

import android.support.annotation.VisibleForTesting
import android.support.annotation.VisibleForTesting.PRIVATE
import me.uport.sdk.universaldid.UniversalDID.method
import me.uport.sdk.universaldid.UniversalDID.registerResolver

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

    /**
     * This universal resolver can't be used for any one particular did but for all [DIDResolver]s
     * that have been added using [registerResolver]
     */
    override val method: String = ""

    /**
     * Checks if any of the registered resolvers can resolve
     */
    override fun canResolve(potentialDID: String): Boolean {
        val resolver = resolvers.values.find {
            it.canResolve(potentialDID)
        }
        return (resolver != null)
    }

    /**
     * Looks for a [DIDResolver] that can resolve the provided [did] either by method if the did contains one or by trial
     *
     * @throws IllegalStateException if the proper resolver is not registered or produces `null`
     * @throws IllegalArgumentException if the given [did] has no `method` but could be resolved by one of the registered resolvers and that one fails with `null`
     */
    override suspend fun resolve(did: String): DIDDocument {
        val (method, _) = parse(did)

        if (method.isBlank()) {
            val resolver = resolvers.values.find {
                it.canResolve(did)
            }
            return resolver?.resolve(did)
                    ?: throw IllegalArgumentException("The provided did ($did) could not be resolved by any of the ${resolvers.size} registered resolvers")
        }  //no else clause, carry on

        if (resolvers.containsKey(method)) {
            return resolvers[method]?.resolve(did)
                    ?: throw IllegalStateException("There DIDResolver for '$method' failed to resolve '$did' for an unknown reason.")
        } else {
            throw IllegalStateException("There is no DIDResolver registered to resolve '$method' DIDs and none of the other ${resolvers.size} registered ones can do it.")
        }
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
    /**
     * The DID method that a particular implementation can resolve
     */
    val method: String

    /**
     * Resolve a given [did] in a coroutine and return the [DIDDocument] or throw an error
     */
    suspend fun resolve(did: String): DIDDocument

    /**
     * Check if the [potentialDID] can be resolved by this resolver.
     */
    fun canResolve(potentialDID: String): Boolean
}