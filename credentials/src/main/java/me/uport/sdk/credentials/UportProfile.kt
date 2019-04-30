package me.uport.sdk.credentials

import me.uport.sdk.jwt.model.JwtPayload


/**
 * This class is used to create a uPort profile
 */
data class UportProfile(

        /**
         * DID associated with the uPort profile
         */
        val did: String,


        /**
         * network id of Ethereum chain of identity
         * eg. 0x4 for rinkeby. It defaults to 0x1 for mainnet.
         */
        val networkId: String?,


        /**
         * A list of verified JWT payloads associated with this uPort profile
         */
        val valid: Collection<JwtPayload>,


        /**
         * A list of invalid JWT tokens associated with this uPort profile
         */
        val invalid: Collection<String>,


        /**
         * The email on the uPort profile
         */
        val email: String? = null,


        /**
         * The name on the uPort profile
         */
        val name: String? = null,


        /**
         * [**optional**]
         * This can hold extra fields for the uPort profile.
         * Use this to provide any extra fields that are not covered by the current version of the SDK
         */
        val extras: Map<String, Any>? = null
)