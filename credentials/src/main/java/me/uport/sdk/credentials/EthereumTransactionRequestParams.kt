@file:Suppress("KDocUnresolvedReference")

package me.uport.sdk.credentials

/**
 * A class that encapsulates the supported parameter types for creating a Ethereum Transaction request.
 *
 * See
 * https://github.com/uport-project/specs/blob/develop/messages/tx.md
 *
 * The parameters in this class get encoded in the JWT that represents such a request.
 *
 * While the JWT can be manually constructed, this class is provided for discoverability
 * and ease of use of frequently used params.
 */
class EthereumTransactionRequestParams(

        /**
         * [**optional**]
         * DID or hex encoded address requested to sign the transaction. If not specified
         * the user will select an account.
         */
        val from: String? = null,

        /**
         * [**REQUIRED**]
         * DID or hex encoded address of the recipient of the transaction. If not specified the
         * transaction will create a contract and a bytecode field must exist.
         */
        val to: String,

        /**
         * [**REQUIRED**]
         * network id of Ethereum chain of identity eg. 0x4 for rinkeby. It defaults to the network encoded
         * in the to if specified as an DID. If not it defaults to 0x1 for mainnet
         */
        val networkId: String,

        /**
         * [**REQUIRED**]
         * hex encoded value in wei
         */
        val value: String,

        /**
         * [**optional**]
         * Callback URL for returning the response to a request (may be deprecated in future)
         *
         * This gets encoded as `callback` in the resulting JWT
         */
        val callbackUrl: String? = null,

        /**
         * [**optional**]
         * Solidity function call eg. transfer(address 0xdeadbeef, uint 5)
         */
        val fn: String? = null,

        /**
         * [**optional**]
         *
         * hex encoded value of integer of the gasPrice used for each paid gas. This is
         * only treated as a recommendation. The client may override this.
         *
         */
        val gasPrice: String? = null,

        /**
         * [**optional**]
         * The DID or DID of the application identity requesting the signature
         *
         */
        val iss: String? = null,

        /**
         * [**optional**]
         * The time of issuance
         *
         */
        val iat: Long? = null,

        /**
         * [**optional**]
         * Expiration time of JWT
         *
         */
        val exp: Long? = null,

        /**
         * [**optional**]
         * This can hold extra fields for the JWT payload representing the request.
         * Use this to provide any extra fields that are not covered by the current version of the SDK
         *
         * The fields contained in [extras] will get overwritten by the named parameters
         * in this class in case of a name collision.
         */
        val extras: Map<String, Any>? = null,

        /**
         * [**optional**] defaults to [DEFAULT_SHARE_REQ_VALIDITY_SECONDS]
         * The validity interval of this request (not of the resulting response),
         * measured in seconds since the moment it is issued.
         */
        val expiresInSeconds: Long? = DEFAULT_ETHEREUM_TRANSACTION_REQ_VALIDITY_SECONDS
)

const val DEFAULT_ETHEREUM_TRANSACTION_REQ_VALIDITY_SECONDS = 600L

/**
 * Converts a [EthereumTransactionRequestParams] object into a map with the required fields for JWT payload
 */
internal fun buildPayloadForEthereumTransactionReq(params: EthereumTransactionRequestParams): MutableMap<String, Any> {
    val payload = params.extras.orEmpty().toMutableMap()

    params.exp?.let { payload["exp"] = it }
    params.iat?.let { payload["iat"] = it }
    params.fn?.let { payload["fn"] = it }
    params.gasPrice?.let { payload["gasPrice"] = it }
    params.iss?.let { payload["iss"] = it }

    params.to.let { payload["to"] = it }
    params.from?.let { payload["from"] = it }
    params.value.let { payload["value"] = it }
    params.callbackUrl?.let { payload["callback"] = it }
    params.networkId.let { payload["net"] = it }

    payload["type"] = JWTTypes.ethtx.name

    return payload
}
