package me.uport.sdk.credentials

/**
 * This is a helper class for building verifiable claim requests
 * [specs](https://github.com/uport-project/specs/blob/develop/messages/sharereq.md#claims-spec)
 * It supports adding multiple verifiable and user-info parameters and returns a map which can be
 * added to the selective disclosure request
 *
 */
class ClaimsRequestParams {

    private val verifiableMap: MutableMap<String, Any> = mutableMapOf()
    private val userInfoMap: MutableMap<String, Any> = mutableMapOf()

    /**
     * This adds a unique record in the verifiable object
     * The [type] param for this method is used as the key for the verifiable
     * Values will be overwritten if this method is called more than once with the same type
     *
     */
    fun addVerifiable(type: String, params: VerifiableParams): ClaimsRequestParams {
        verifiableMap.put(type, params.getMap())
        return this
    }

    /**
     * This adds a unique record in the user_info object
     * The [type] param for this method is used as the key for the user_info
     * Values will be overwritten if this method is called more than once with the same type
     *
     */
    fun addUserInfo(type: String, params: UserInfoParams): ClaimsRequestParams {
        userInfoMap.put(type, params.getMap())
        return this
    }

    /**
     * Returns the final map which can be used in a selective disclosure request
     *
     */
    fun build(): Map<String, Any?>? {
        val payload: MutableMap<String, Any> = mutableMapOf()
        payload["verifiable"] = verifiableMap
        payload["user_info"] = userInfoMap
        return payload
    }
}

/**
 * This is a helper class for adding properties and values to a verifiable
 *
 */
data class VerifiableParams(

    /**
     * [**optional**]
     * Short string explaining why you need this
     *
     */
    private val reason: String,

    /**
     * [**optional**]
     * Indicate if this claim is essential
     *
     */
    private val essential: Boolean = false
) {

    private val params: MutableMap<String, Any> = mutableMapOf()
    private val issuers: MutableList<Issuer> = mutableListOf()

    /**
     * This adds records to the array of issuers
     *
     */
    fun addIssuer(did: String, url: String? = null): VerifiableParams {
        issuers.add(Issuer(did, url))
        return this
    }

    /**
     * Returns a Mutable map of the a verifiable's params
     *
     */
    fun getMap(): MutableMap<String, Any> {
        params["iss"] = issuers
        params["reason"] = reason
        params["essential"] = essential
        return params
    }
}

/**
 * This is a helper class for adding properties and values to user_info
 *
 */
data class UserInfoParams(

    /**
     * [**optional**]
     * Short string explaining why you need this
     *
     */
    private val reason: String,

    /**
     * [**optional**]
     * Indicate if this claim is essential
     *
     */
    private val essential: Boolean = false

) {

    private val params: MutableMap<String, Any> = mutableMapOf()

    /**
     * Returns a Mutable map of the user_info params
     *
     */
    fun getMap(): MutableMap<String, Any> {
        params["reason"] = reason
        params["essential"] = essential
        return params
    }
}


/**
 * This is a helper class for adding properties and values to issuer
 *
 */
data class Issuer(

    /**
     * [**required**]
     * The DID of allowed issuer of claims
     *
     */
    private val did: String,

    /**
     * [**optional**]
     * The URL for obtaining the claim
     *
     */
    private val url: String? = null
)