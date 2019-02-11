@file:Suppress("KDocUnresolvedReference")

package me.uport.sdk.credentials

/**
 * A class that encapsulates the supported parameter types for creating a Personal Sign request.
 *
 * See
 * https://github.com/uport-project/specs/blob/develop/messages/personalsignreq.md
 *
 * The parameters in this class get encoded in the JWT that represents such a request.
 *
 * While the JWT can be manually constructed, this class is provided for discoverability
 * and ease of use of frequently used params.
 */
class PersonalSignRequestParams(

        /**
         * [**required**]
         * A string containing the message to be signed.
         */
        val data: String,

        /**
         * [**optional**]
         * Callback URL for returning the response to a request (may be deprecated in future)
         *
         * This gets encoded as `callback` in the resulting JWT
         */
        val callbackUrl: String? = null,

        /**
         * [**optional**]
         * The DID of the identity you want to sign the Verified Claim.
         *
         * This can be pre-requested with a selective disclosure request
         */
        val riss: String? = null,

        /**
         * [**optional**]
         * Hex encoded address requested to sign the transaction.
         * If not specified the user will select an account.
         */
        val from: String? = null,

        /**
         * network id of Ethereum chain of identity
         * eg. 0x4 for rinkeby. It defaults to 0x1 for mainnet.
         */
        val networkId: String? = null,

        /**
         * Array of Verified Claims JWTs or IPFS hash of JSON encoded equivalent about the iss of this message.
         * See Issuer Claims and Verified Clais
         */
        val vc: Collection<String>? = null,

        /**
         * [**optional**] defaults to [DEFAULT_SHARE_REQ_VALIDITY_SECONDS]
         * The validity interval of this request (not of the resulting response),
         * measured in seconds since the moment it is issued.
         */
        val expiresInSeconds: Long? = DEFAULT_PERSONAL_SIGN_REQ_VALIDITY_SECONDS,


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

const val DEFAULT_PERSONAL_SIGN_REQ_VALIDITY_SECONDS = 600L

/**
 * Converts a [PersonalSignRequestParams] object into a map with the required fields for JWT payload
 */
internal fun buildPayloadForPersonalSignReq(params: PersonalSignRequestParams): MutableMap<String, Any> {
    val payload = params.extras.orEmpty().toMutableMap()

    payload["data"] = params.data
    params.riss?.let { payload["riss"] = it }
    params.from?.let { payload["from"] = it }
    params.networkId?.let { payload["net"] = it }
    params.vc?.let { payload["vc"] = it }
    params.callbackUrl?.let { payload["callback"] = it }

    payload["type"] = JWTTypes.personalSigReq.name

    return payload
}