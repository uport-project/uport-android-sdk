@file:Suppress("KDocUnresolvedReference")

package me.uport.sdk.credentials

/**
 * A class that encapsulates the supported parameter types for creating a Verified Claim request.
 *
 * See
 * https://github.com/uport-project/specs/blob/develop/messages/verificationreq.md
 *
 * The parameters in this class get encoded in the JWT that represents such a request.
 *
 * While the JWT can be manually constructed, this class is provided for discoverability
 * and ease of use of frequently used params.
 */
class VerifiedClaimRequestParams(

        /**
         * [**required**]
         * An unsigned claim that is requested to be signed.
         * This should exactly match the claim of the signed Verified Claim response.
         */
        val unsignedClaim: Map<String, Any?>,

        /**
         * [**optional**]
         * The DID of the identity that the claim is about
         */
        val sub: String? = null,

        /**
         * [**optional**]
         * Callback URL for returning the response to a request (may be deprecated in future)
         *
         * This gets encoded as `callback` in the resulting JWT
         */
        val callbackUrl: String? = null,

        /**
         * [**optional**]
         *
         * The DID or URL of the audience of the JWT.
         * The uPort app will not accept any JWT that has someone else as the audience
         *
         */
        val aud: String? = null,

        /**
         * [**optional**]
         * The DID of the identity you want to sign the Verified Claim.
         *
         * This can be pre-requested with a selective disclosure request
         */
        val riss: String? = null,

        /**
         * [**optional**]
         * Requested expiry time in seconds
         *
         * TODO: clarify if this is interval or timestamp
         */
        val rexp: Long? = null,


        /**
         * [**optional**]
         * The self signed claims for the [iss] of this message.
         *
         * Either as a [Map] of claim types for self signed claims eg:
         * ```
         * mapOf(
         *   "name" to "Some Corp Inc",
         *   "url" to "https://somecorp.example",
         *   "image" to mapOf("/" to "/ipfs/QmSCnmXC91Arz2gj934Ce4DeR7d9fULWRepjzGMX6SSazB")
         * )
         * ```
         * or the IPFS Hash of a JSON encoded equivalent.
         */
        val issc: Map<String, Any>? = null,

        /**
         * [**optional**]
         * A collection of Verified Claims JWTs or IPFS hash of JSON encoded equivalent about the [iss] of this message.
         * TODO: clarify what's the difference between one of the claims in [vc] and [issc]
         */
        val vc: Collection<String>? = null,

        /**
         * [**optional**] defaults to [DEFAULT_SHARE_REQ_VALIDITY_SECONDS]
         * The validity interval of this request (not of the resulting response),
         * measured in seconds since the moment it is issued.
         */
        val expiresInSeconds: Long? = DEFAULT_VERIFIED_CLAIM_REQ_VALIDITY_SECONDS,

        /**
         * [**optional**]
         * This can hold extra fields for the JWT payload representing the request.
         * Use this to provide any extra fields that are not covered by the current version of the SDK
         *
         * The fields contained in [extras] will get overwritten by the named parameters
         * in this class in case of a name collision.
         */
        val extras: Map<String, Any>? = null
)

const val DEFAULT_VERIFIED_CLAIM_REQ_VALIDITY_SECONDS = 600L

/**
 * Converts a [VerifiedClaimRequestParams] object into a map with the required fields for JWT payload
 */
internal fun buildPayloadForVerifiedClaimReq(params: VerifiedClaimRequestParams): MutableMap<String, Any> {
    val payload = params.extras.orEmpty().toMutableMap()

    params.riss?.let { payload["riss"] = it }
    params.vc?.let { payload["vc"] = it }
    params.callbackUrl?.let { payload["callback"] = it }
    params.sub?.let { payload["sub"] = it }
    params.aud?.let { payload["aud"] = it }
    params.rexp?.let { payload["rexp"] = it }
    params.issc?.let { payload["issc"] = it }

    payload["type"] = JWTTypes.verReq.name
    payload["unsignedClaim"] = params.unsignedClaim

    return payload
}