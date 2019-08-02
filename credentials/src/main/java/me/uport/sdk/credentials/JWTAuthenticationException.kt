package me.uport.sdk.credentials

import me.uport.sdk.jwt.InvalidJWTException

/**
 * Thrown when a JWT authentication fails invalid because the issuer in the response and request do not match,
 * the request type is not `shareReq`
 * the request token is not in the response token
 */
class JWTAuthenticationException(message: String) : InvalidJWTException(message)
