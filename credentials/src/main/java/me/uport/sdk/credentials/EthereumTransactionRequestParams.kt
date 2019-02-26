@file:Suppress("KDocUnresolvedReference")

package me.uport.sdk.credentials

import org.kethereum.extensions.toHexStringNoPrefix
import org.kethereum.extensions.toHexStringZeroPadded
import java.math.BigInteger

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
         * Hex encoded address requested to sign the transaction. If not specified
         * the user will select an account.
         */
        val from: String? = null,

        /**
         * [**REQUIRED**]
         * Hex encoded address of the recipient of the transaction. If not specified the
         * transaction will create a contract and a bytecode field must exist.
         */
        val to: String,

        /**
         *
         * [**REQUIRED**]
         * network ID of the ethereum chain the transaction will be sent to.
         * See [Networks.kt]
         *
         */
        val networkId: String,

        /**
         * [**REQUIRED**]
         * transaction value in wei
         */
        val value: BigInteger,

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
        val gasPrice: BigInteger? = null,

        /**
         * [**optional**]
         * The value of gas
         *
         */
        val gas: BigInteger? = null,

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
         * [**optional**] defaults to [DEFAULT_ETHEREUM_TRANSACTION_REQ_VALIDITY_SECONDS]
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

    params.fn?.let { payload["fn"] = it }
    params.gasPrice?.toHexStringZeroPadded(1).toString().let { payload["gasPrice"] = it }
    params.gas?.toHexStringZeroPadded(1).toString().let { payload["gas"] = it }

    params.to.let { payload["to"] = it }
    params.from?.let { payload["from"] = it }
    params.value.toHexStringZeroPadded(1).let { payload["value"] = it }
    params.callbackUrl?.let { payload["callback"] = it }
    params.networkId.let { payload["net"] = it }

    payload["type"] = JWTTypes.ethtx.name

    return payload
}
