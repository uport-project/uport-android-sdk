package me.uport.sdk.jwt

/**
 * Thrown when the a JWT does not seem to have the proper format
 */
class JWTEncodingException(message: String) : IllegalArgumentException(message)

/**
 * Thrown when a JWT is invalid either because it is expired, not valid yet or the signature doesn't match
 */
class InvalidJWTException(message: String) : IllegalStateException(message)
