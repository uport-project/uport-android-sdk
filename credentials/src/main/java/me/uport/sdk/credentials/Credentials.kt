package me.uport.sdk.credentials

import android.support.annotation.Keep
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
     * Supported (known) types of requests/responses
     */
    @Keep
    @Suppress("EnumEntryName")
    enum class RequestType {
        /**
         * a selective disclosure request
         * See also:  https://github.com/uport-project/specs/blob/develop/messages/sharereq.md
         */
        shareReq,

        /**
         * a selective disclosure response
         * See also: https://github.com/uport-project/specs/blob/develop/messages/shareresp.md
         */
        shareResp,

        /**
         * See also:  https://github.com/uport-project/specs/blob/develop/messages/verificationreq.md
         */
        verReq,

        /**
         * See also:  https://github.com/uport-project/specs/blob/develop/messages/signtypeddata.md
         */
        eip712Req,

        /**
         * See also:  https://github.com/uport-project/specs/blob/develop/messages/tx.md
         */
        ethtx
    }

    @Keep
    @Suppress("EnumEntryName")
    enum class RequestAccountType {
        general,
        segregated,
        keypair,
        devicekey,
        none
    }

    /**
     * A class that encapsulates the supported parameter types for creating a SelectiveDisclosureRequest.
     *
     * A selective disclosure request is just a JWT with some required fields
     * (described [here](https://github.com/uport-project/specs/blob/develop/messages/sharereq.md) )
     * The parameters in this class get encoded in the JWT that represents such a request.
     *
     * While the JWT can be manually constructed, this class is provided for discoverability
     * and ease of use of frequently used params.
     */
    class SelectiveDisclosureRequestParams(
            /**
             * [**required**]
             * a list of attributes for which you are requesting credentials.
             * Ex. [ 'name', 'country' ]
             */
            val requested: List<String>,

            /**
             * [**optional**]
             * A list of signed claims being requested.
             * This is semantically similar to the [requested] field
             * but the response should contain signatures as well.
             */
            val verified: List<String>?,

            /**
             * [**required**]
             * the url that can receive the response to this request.
             * TODO: detail how that URL should be handled by the APP implementing this SDK
             *
             * This gets encoded as `callback` in the JWT payload
             */
            val callbackUrl: String,

            /**
             * [**optional**]
             * The Ethereum network ID if it is relevant for this request.
             *
             * This gets encoded as `net` in the JWT payload
             */
            val networkId: String?,


            /**
             * [**optional**]
             * If this request implies a particular kind of account. This defaults to [RequestAccountType.none]
             *
             * This gets encoded as `act` in the JWT payload
             *
             * @see [RequestAccountType]
             */
            val accountType: RequestAccountType? = RequestAccountType.none,

            /**
             * [**optional**] defaults to [DEFAULT_SHARE_REQ_VALIDITY_SECONDS]
             * The validity interval of this request, measured in seconds since the moment it is issued.
             */
            val expiresInSeconds: Long? = DEFAULT_SHARE_REQ_VALIDITY_SECONDS,


            //omitting the "notifications" permission because it has no relevance on android.
            // It may be worth adding for direct interop with iOS but that is unclear now

            /**
             * [**optional**]
             * This can hold extra fields for the JWT payload representing the request.
             * Use this to provide any of the extra fields described in the
             * [specs](https://github.com/uport-project/specs/blob/develop/messages/sharereq.md)
             *
             * The fields contained in [extras] will get overwritten by the named parameters
             * in this class in case of a name collision.
             */
            val extras: Map<String, Any>?
    )

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

        private const val DEFAULT_SHARE_REQ_VALIDITY_SECONDS = 600L
    }

}