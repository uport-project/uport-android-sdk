package me.uport.sdk.credentials

import me.uport.sdk.core.ITimeProvider
import me.uport.sdk.core.SystemTimeProvider
import me.uport.sdk.credentials.model.CredentialParams
import me.uport.sdk.credentials.model.PresentationParams
import me.uport.sdk.jwt.InvalidJWTException
import me.uport.sdk.jwt.JWTTools
import me.uport.sdk.jwt.JWTTools.Companion.DEFAULT_JWT_VALIDITY_SECONDS
import me.uport.sdk.jwt.JWTUtils.Companion.normalizeKnownDID
import me.uport.sdk.jwt.model.JwtHeader.Companion.ES256K
import me.uport.sdk.jwt.model.JwtHeader.Companion.ES256K_R
import me.uport.sdk.jwt.model.JwtPayload
import me.uport.sdk.signer.Signer

/**
 * The [Credentials] class should allow you to create the signed payloads used in uPort including
 * verifiable claims and signed mobile app requests (ex. selective disclosure requests
 * for user data). It should also provide signature verification over signed payloads.
 */
class Credentials(
    private val did: String,
    private val signer: Signer,
    private val clock: ITimeProvider = SystemTimeProvider
) {

    /**
     *  Creates a [Selective Disclosure Request JWT](https://github.com/uport-project/specs/blob/develop/messages/sharereq.md)
     *
     * Example:
     * ```
     *  val reqParams = SelectiveDisclosureRequestParams(
     *                      requested = listOf("name", "country"),
     *                      callbackUrl = "https://myserver.com"
     *                  )
     *  val jwt = credentials.createDisclosureRequest(reqParams)
     *
     *  // ... send jwt to the relevant party and expect a callback with the response at https://myserver.com
     *
     *  ```
     */
    suspend fun createDisclosureRequest(params: SelectiveDisclosureRequestParams): String {
        val payload = buildPayloadForShareReq(params)
        return this.signJWT(payload, params.expiresInSeconds ?: DEFAULT_SHARE_REQ_VALIDITY_SECONDS)
    }

    /**
     * Create a JWT requesting an eth_sign/personal_sign from a user of another uPort client app.
     *
     * See https://github.com/uport-project/specs/blob/develop/messages/personalsignreq.md
     *
     * Example:
     * ```
     *  val reqParams = PersonalSignRequestParams(
     *                      data = "This is the message to be signed",
     *                      callbackUrl = "https://myserver.com"
     *                  )
     *  val jwt = credentials.createPersonalSignRequest(reqParams)
     *
     *  // ... send jwt to the relevant party and expect a callback with the response at https://myserver.com
     *
     *  ```
     */
    suspend fun createPersonalSignRequest(params: PersonalSignRequestParams): String {
        val payload = buildPayloadForPersonalSignReq(params)
        return this.signJWT(
            payload,
            params.expiresInSeconds ?: DEFAULT_PERSONAL_SIGN_REQ_VALIDITY_SECONDS
        )
    }

    /**
     * Create a JWT requesting a verified claim from a user of another uPort client app.
     *
     * See https://github.com/uport-project/specs/blob/develop/messages/verificationreq.md
     *
     * Example:
     * ```
     *  val reqParams = VerifiedClaimRequestParams(
     *                      unsignedClaim = mapOf(
     *                          "Citizen of city X" to mapOf(
     *                              "Allowed to vote" to true,
     *                              "Document" to "QmZZBBKPS2NWc6PMZbUk9zUHCo1SHKzQPPX4ndfwaYzmPW"
     *                          )
     *                      ),
     *                      callbackUrl = "https://myserver.com"
     *                  )
     *  val jwt = credentials.createVerificationSignatureRequest(reqParams)
     *
     *  // ... send jwt to the relevant party and expect a callback with the response at https://myserver.com
     *
     *  ```
     */
    suspend fun createVerificationSignatureRequest(params: VerifiedClaimRequestParams): String {
        val payload = buildPayloadForVerifiedClaimReq(params)
        return this.signJWT(
            payload,
            params.expiresInSeconds ?: DEFAULT_VERIFIED_CLAIM_REQ_VALIDITY_SECONDS
        )
    }

    /**
     * Create a JWT requesting ethereum transaction signing from a user of another uPort client app.
     *
     * See https://github.com/uport-project/specs/blob/develop/messages/tx.md
     *
     * Example:
     * ```
     *  val params = EthereumTransactionRequestParams(
     *                      to = issuerAddress,
     *                      value = BigInteger("1"),
     *                      callbackUrl = "https://myserver.com",
     *                      networkId = Networks.rinkeby.networkId
     *                   )
     *
     *  val jwt = credentials.createEthereumTransactionRequest(params)
     *
     *  // ... send jwt to the relevant party and expect a callback with the response at https://myserver.com
     *
     *  ```
     */
    suspend fun createEthereumTransactionRequest(params: EthereumTransactionRequestParams): String {
        val payload = buildPayloadForEthereumTransactionReq(params)
        return this.signJWT(
            payload,
            params.expiresInSeconds ?: DEFAULT_ETHEREUM_TRANSACTION_REQ_VALIDITY_SECONDS
        )
    }


    /**
     * Creates a JWT with a signed claim.
     *
     * @param sub **REQUIRED** a valid DID for the subject of credential
     * @param claim **REQUIRED** claim about subject single key value or key mapping
     *              to object with multiple values
     * @param callbackUrl **OPTIONAL** the URL that receives the response
     * @param expiresInSeconds **OPTIONAL** number of seconds of validity of the claim.
     * @param verifiedClaims **OPTIONAL** a list of verified claims which can be about anything
     *                          related to the claim and in most cases it is related to the issuer
     *
     *  ```
     */
    suspend fun createVerification(
        sub: String,
        claim: Map<String, Any>,
        callbackUrl: String? = null,
        verifiedClaims: Collection<String>? = null,
        expiresInSeconds: Long? = 600L
    ): String {

        val payload = mutableMapOf<String, Any>()
        payload["sub"] = sub
        payload["claim"] = claim
        payload["vc"] = verifiedClaims ?: emptyList<String>()
        payload["callback"] = callbackUrl ?: ""

        return this.signJWT(payload, expiresInSeconds ?: 600L)
    }

    /**
     * Creates a W3C compliant verifiable credential serialized as a JWT ().
     *
     * @param subject the subject of the claim in the [credential]
     *      This becomes the `sub` field in the JWT
     * @param credential the credential details; contains the claim being made about the [subject]
     *      This becomes the `vc` field in the JWT
     * @param notValidBefore [**optional**] the UNIX timestamp that marks the beginning of the validity period.
     *      If this is not specified, the current timestamp is used as given by [clock]
     *      This becomes the `nbf` field of the JWT. It is measured in seconds.
     * @param validityPeriod [**optional**] the number of seconds of validity of this credential.
     *      This influences the `exp` field in the JWT
     *      If this is negative, there will be no expiry date set on the credential.
     * @param audience [**optional**] the intended audience of this credential.
     *      This results in the `aud` field of the JWT. If it is `null`, no audience will be set for the JWT.
     * @param id [**optional**] the ID of this credential.
     *      This becomes the `jti` field in the resulting JWT.
     */
    suspend fun createVerifiableCredential(
        subject: String,
        credential: CredentialParams,
        notValidBefore: Long = clock.nowMs() / 1000L,
        validityPeriod: Long = -1L,
        audience: String? = null,
        id: String? = null
    ): String {

        val payload = mutableMapOf<String, Any>()
        payload["sub"] = subject

        //add defaults if they are not set
        val processedCredential = credential.copy(
            context = credential.context.toMutableSet().apply { add("https://www.w3.org/2018/credentials/v1") }.toList(),
            type = credential.type.toMutableSet().apply { add("VerifiableCredential") }.toList()
        )

        payload["vc"] = processedCredential
        payload["nbf"] = notValidBefore
        payload["iat"] = notValidBefore //for backward compatibility
        if (validityPeriod >= 0) {
            val exp = notValidBefore + validityPeriod
            payload["exp"] = exp
        }
        if (audience != null) {
            payload["aud"] = audience
        }
        if (id != null) {
            payload["jti"] = id
        }

        return this.signJWT(
            payload = payload,
            expiresInSeconds = validityPeriod,
            algorithm = ES256K
        )
    }

    suspend fun createPresentation(
        vp: PresentationParams,
        notValidBefore: Long = clock.nowMs() / 1000L,
        validityPeriod: Long = -1L,
        audience: String? = null,
        id: String? = null
    ): String {
        val payload = mutableMapOf<String, Any>()

        val processedPresentation = vp.copy(
            context = vp.context.toMutableSet().apply { add("https://www.w3.org/2018/credentials/v1") }.toList(),
            type = vp.type.toMutableSet().apply { add("VerifiablePresentation") }.toList()
        )
        payload["vp"] = processedPresentation
        payload["nbf"] = notValidBefore
        payload["iat"] = notValidBefore //for backward compatibility
        if (validityPeriod >= 0) {
            val exp = notValidBefore + validityPeriod
            payload["exp"] = exp
        }
        if (audience != null) {
            payload["aud"] = audience
        }
        if (id != null) {
            payload["jti"] = id
        }

        return this.signJWT(
            payload = payload,
            expiresInSeconds = validityPeriod,
            algorithm = ES256K
        )
    }


    /**
     * Verify and return profile from a
     * [Selective Disclosure Response JWT](https://github.com/uport-project/specs/blob/develop/messages/shareresp.md).
     *
     * @param token **REQUIRED** The JWT response token from a selective disclosure request
     *
     * @return a [UportProfile] object
     */
    suspend fun verifyDisclosure(token: String): UportProfile {

        val payload = JWTTools().verify(token, audience = this.did)

        val valid = mutableListOf<JwtPayload>()
        val invalid = mutableListOf<String>()

        payload.verified?.forEach {
            try {
                valid.add(JWTTools().verify(it, audience = this.did))
            } catch (e: InvalidJWTException) {
                e.printStackTrace()
                invalid.add(it)
            }
        }

        val networkId = payload.net ?: JWTTools().decode(payload.req ?: "").second.net

        return UportProfile(
            payload.iss,
            networkId,
            valid,
            invalid,
            payload.own?.get("email"),
            payload.own?.get("name"),
            JWTTools().decodeRaw(token).second
        )
    }

    /**
     * Authenticates [Selective Disclosure Response JWT](https://github.com/uport-project/specs/blob/develop/messages/shareresp.md) from uPort
     * client as part of the [Selective Disclosure Flow](https://github.com/uport-project/specs/blob/develop/flows/selectivedisclosure.md).
     *
     * It Verifies and parses the given response token and verifies the challenge response flow.
     *
     * @param token **REQUIRED** a valid JWT response token
     * @returns  a verified [JwtPayload]
     * @throws [JWTAuthenticationException] when the challenge is failed or when the request token is unavailable
     *
     */
    suspend fun authenticateDisclosure(token: String): JwtPayload {
        val payload = JWTTools().verify(token, auth = true, audience = this.did)

        if (payload.req == null) {
            throw JWTAuthenticationException("Challenge was not included in response")
        }

        val challenge = JWTTools().verify(payload.req ?: "")

        if (challenge.iss != this.did) {
            throw JWTAuthenticationException("Challenge issuer does not match current identity: ${challenge.iss} != ${this.did}")
        }

        if (challenge.type != JWTTypes.shareReq.name) {
            throw JWTAuthenticationException("Challenge payload type invalid: ${challenge.type}")
        }

        return payload
    }


    /**
     *  Creates a JWT using the given [payload], issued and signed using the [did] and [signer]
     *  fields of this [Credentials] instance.
     *
     *  @param payload a map detailing the payload of the resulting JWT
     *  @param expiresInSeconds _optional_ number of seconds of validity of the JWT. This parameter
     *              is ignored if the [payload] already contains an `exp` field
     */
    suspend fun signJWT(
        payload: Map<String, Any>,
        expiresInSeconds: Long = DEFAULT_JWT_VALIDITY_SECONDS,
        algorithm: String? = null
    ): String {
        val normDID = normalizeKnownDID(this.did)
        val alg = algorithm ?: if (normDID.startsWith("did:uport:")) ES256K else ES256K_R
        return JWTTools(clock).createJWT(
            payload,
            normDID,
            this.signer,
            expiresInSeconds = expiresInSeconds,
            algorithm = alg
        )
    }

}

