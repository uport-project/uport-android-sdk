package me.uport.sdk.jwt

import java.security.SignatureException

class JWTEncodingException(message: String) : IllegalArgumentException(message)
class InvalidJWTException(message: String) : IllegalStateException(message)
class InvalidSignatureException(message: String) : SignatureException(message)
