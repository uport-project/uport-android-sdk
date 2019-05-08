package me.uport.sdk.jwt

/**
 * Thrown when the a JWT does not seem to have the proper format
 */
class JWTEncodingException(message: String) : IllegalArgumentException(message)

/**
 * Thrown when a JWT is invalid either because it is expired, not valid yet or the signature doesn't match
 */
open class InvalidJWTException(message: String) : IllegalStateException(message)

/**
 * Thrown when a JWT authentication fails invalid because the issuer in the response and request do not match,
 * the request type is not [shareReq]
 * the request token is not in the response token
 */
class JWTAuthenticationException(message: String) : InvalidJWTException(message)
