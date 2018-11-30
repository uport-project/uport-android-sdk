package me.uport.sdk.credentials

import android.support.annotation.Keep

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
         * a simple_list of attributes for which you are requesting credentials.
         * Ex. [ 'name', 'country' ]
         */
        val requested: List<String>,

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
         * A simple_list of signed claims being requested.
         * This is semantically similar to the [requested] field
         * but the response should contain signatures as well.
         */
        val verified: List<String>? = null,

        /**
         * [**optional**]
         * The Ethereum network ID if it is relevant for this request.
         *
         * This gets encoded as `net` in the JWT payload
         */
        val networkId: String? = null,


        /**
         * [**optional**]
         * If this request implies a particular kind of account.
         * This defaults to [RequestAccountType.general] (user choice)
         *
         * This gets encoded as `act` in the JWT payload
         *
         * @see [RequestAccountType]
         */
        val accountType: RequestAccountType? = RequestAccountType.general,

        /**
         * [**optional**]
         * A simple_list of signed claims about the issuer, usually signed by 3rd parties.
         */
        val vc: List<String>? = null,

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
        val extras: Map<String, Any>? = null
)

/**
 * Ethereum account type that can be requested by a dApp during selective disclosure:
 *
 * * [general] users choice (default)
 * * [segregated] a unique smart contract based account will be created for requesting app
 * * [keypair] a unique keypair based account will be created for requesting app
 * * [devicekey] request a new device key for a
 * [Private Chain Account](https://github.com/uport-project/specs/blob/develop/messages/privatechain.md)
 * * [none] no account is returned
 */
@Keep
@Suppress("EnumEntryName")
enum class RequestAccountType {
    general,
    segregated,
    keypair,
    devicekey,
    none
}

const val DEFAULT_SHARE_REQ_VALIDITY_SECONDS = 600L

/**
 * Converts a [SelectiveDisclosureRequestParams] object into a map with the required fields for JWT payload
 */
internal fun buildPayloadForShareReq(params: SelectiveDisclosureRequestParams): MutableMap<String, Any> {
    val payload = params.extras.orEmpty().toMutableMap()

    payload["callback"] = params.callbackUrl
    payload["requested"] = params.requested
    params.verified?.let { payload["verified"] = it }
    params.vc?.let { payload["vc"] = it }
    params.networkId?.let { payload["net"] = it }
    params.accountType?.let { payload["act"] = it.name }

    payload["type"] = RequestType.shareReq.name

    return payload
}