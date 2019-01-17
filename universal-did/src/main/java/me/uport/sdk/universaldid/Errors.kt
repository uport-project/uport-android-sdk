package me.uport.sdk.universaldid

/**
 * Generic error class for exceptions that can occur during DID resolution
 */
open class DidResolverError(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * The resolver didn't encounter any other obvious error but the resulting DID document is blank.
 */
class BlankDocumentError(message: String) : DidResolverError(message)