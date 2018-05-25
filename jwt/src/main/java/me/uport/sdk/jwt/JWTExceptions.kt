package me.uport.sdk.jwt

import java.security.SignatureException

class InvalidJWTException(message: String): IllegalStateException(message)
class InvalidSignatureException(message: String): SignatureException(message)
