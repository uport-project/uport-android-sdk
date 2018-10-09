package me.uport.sdk.jwt

import com.uport.sdk.signer.Signer
import com.uport.sdk.signer.getJoseEncoded
import com.uport.sdk.signer.signJWT
import me.uport.sdk.core.utf8
import me.uport.sdk.jwt.model.JwtHeader

/**
 * Abstracts the signature to a recoverable/non-recoverable JOSE encoding based on the jwt header or algorithm provided during construction
 *
 * It supports "ES256K" and "ES256K-R" signing methods
 */
class JWTSignerAlgorithm(private val jwtHeader: JwtHeader) {

    constructor(algorithm: String) : this(JwtHeader(alg = algorithm))

    suspend fun sign(payload: String, signer: Signer): String {

        val signatureData = signer.signJWT(payload.toByteArray(utf8))

        return when (jwtHeader.alg) {
            JwtHeader.ES256K -> signatureData.getJoseEncoded(false)
            JwtHeader.ES256K_R -> signatureData.getJoseEncoded(true)
            else -> throw JWTEncodingException("Unknown algorithm (${jwtHeader.alg}) requested for signing")
        }
    }
}