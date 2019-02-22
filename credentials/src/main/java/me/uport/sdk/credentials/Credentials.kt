package me.uport.sdk.credentials

import android.support.annotation.VisibleForTesting
import android.support.annotation.VisibleForTesting.PRIVATE
import com.uport.sdk.signer.Signer
import me.uport.mnid.MNID
import me.uport.sdk.core.ITimeProvider
import me.uport.sdk.core.SystemTimeProvider
import me.uport.sdk.jwt.JWTTools
import me.uport.sdk.jwt.JWTTools.Companion.DEFAULT_JWT_VALIDITY_SECONDS
import me.uport.sdk.jwt.model.JwtHeader

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
        return this.signJWT(payload, params.expiresInSeconds
                ?: DEFAULT_PERSONAL_SIGN_REQ_VALIDITY_SECONDS)
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
        return this.signJWT(payload, params.expiresInSeconds
                ?: DEFAULT_VERIFIED_CLAIM_REQ_VALIDITY_SECONDS)
    }


    /**
     *  Creates a JWT using the given [payload], issued and signed using the [did] and [signer]
     *  fields of this [Credentials] instance.
     *
     *  @param payload a map detailing the payload of the resulting JWT
     *  @param expiresInSeconds _optional_ number of seconds of validity of the JWT. This parameter
     *              is ignored if the [payload] already contains an `exp` field
     */
    suspend fun signJWT(payload: Map<String, Any>, expiresInSeconds: Long = DEFAULT_JWT_VALIDITY_SECONDS): String {
        val normDID = normalizeKnownDID(this.did)
        val alg = if (normDID.startsWith("did:uport:")) JwtHeader.ES256K else JwtHeader.ES256K_R
        return JWTTools(clock).createJWT(payload, normDID, this.signer, expiresInSeconds = expiresInSeconds, algorithm = alg)
    }

    companion object {

        /**
         * Attempts to normalize a [potentialDID] to a known format.
         *
         * This will transform an ethereum address into an ethr-did and an MNID string into a uport-did
         */
        @VisibleForTesting(otherwise = PRIVATE)
        internal fun normalizeKnownDID(potentialDID: String): String {

            //ignore if it's already a did
            if (potentialDID.matches("^did:(.*)?:.*".toRegex()))
                return potentialDID

            //match an ethereum address
            "^(0[xX])*([0-9a-fA-F]{40})".toRegex().find(potentialDID)?.let {
                val (_, hexDigits) = it.destructured
                return "did:ethr:0x$hexDigits"
            }

            //match an MNID
            if (MNID.isMNID(potentialDID)) {
                return "did:uport:$potentialDID"
            }

            return potentialDID
        }
    }

}