package me.uport.sdk.transport

/**
 * Generic class for handling various response types
 **
 */
sealed class UriResponse

/**
 * Data Class to handle all JWT response types
 **
 */
class JWTUriResponse(val token: String) : UriResponse()

/**
 * Data Class to handle all Transaction Hashcode response types
 **
 */
class HashCodeUriResponse(val token: String) : UriResponse()

/**
 * Data Class to handle response errors
 **
 */
class ErrorUriResponse(val message: String) : UriResponse()