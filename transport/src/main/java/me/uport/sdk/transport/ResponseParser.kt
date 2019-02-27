package me.uport.sdk.transport

import android.content.Intent
import android.net.Uri
import java.net.URI

/**
 * This class should be used when receiving a deeplink callback
 * It contains helpers for interpreting responses
 *
 * API volatility: __high__
 *
 */
object ResponseParser {

    //language=RegExp
    private val fragmentMatcher = ".*[&#]*((?:access_token=|verification=|typedDataSig=|personalSig=)([A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-]+))&*.*$".toRegex()

    //language=RegExp for ethereum transaction signing responses only
    private val txRequestFragmentMatcher = ".*[&#]*(tx=((0x)?[A-Fa-f0-9]{64})).*$".toRegex()

    //language=RegExp
    private val errorMatcher = ".*[&#]*(error=(.*))&*.*$".toRegex()

    /**
     * Given a deep link uri, this method tries to extract a JWT response from it.
     * The expected format is a string of the form "access_token=<JWT>" appended to the `fragment`
     * part of a URI
     *
     * @param deeplinkURI the deeplink URI
     * @return a JWT token string if one could be extracted or `null` otherwise
     *
     * @throws IllegalArgumentException if the URI can't be parsed or does not match the expected format
     * @throws RuntimeException if the deeplink has an error block in the fragment
     */
    fun extractTokenFromRedirectUri(deeplinkURI: String): UriResponse {
        val uriFragment = try {
            URI.create(deeplinkURI).fragment
        } catch (ex: Exception) {
            null
        } ?: throw IllegalArgumentException("Cannot parse URI")

        val uriResponse = matchJWTUri(uriFragment)
                ?: matchHashcodeUri(uriFragment)
                ?: matchErrorUri(uriFragment)
                ?: throw IllegalArgumentException("URI does not match known response format")

        return uriResponse
    }

    /**
     * This method tries to match the [uriFragment] to extract the token.
     * [JWTUriResponse] is returned if the matching is successful
     * It returns [null] if matching fails
     **
     */
    private fun matchJWTUri(uriFragment: String): UriResponse? {
        val matchResult = fragmentMatcher.matchEntire(uriFragment)
        if (matchResult != null) {

            val (_, token) = matchResult.destructured

            return JWTUriResponse(token = token)
        }
        return null
    }


    /**
     * This method tries to match the [uriFragment] to extract any error messages.
     * [ErrorUriResponse] is returned if the matching is successful
     * It returns [null] if matching fails
     **
     */
    private fun matchErrorUri(uriFragment: String): UriResponse? {
        val matchResult = errorMatcher.matchEntire(uriFragment)
        if (matchResult != null) {

            val (_, message) = matchResult.destructured

            return ErrorUriResponse(message = message)
        }
        return null
    }

    /**
     * This method tries to match the [uriFragment] to extract the token.
     * [HashCodeUriResponse] is returned if the matching is successful
     * It returns [null] if matching fails
     **
     */
    private fun matchHashcodeUri(uriFragment: String): UriResponse? {
        val matchResult = txRequestFragmentMatcher.matchEntire(uriFragment)
        if (matchResult != null) {

            val (_, token) = matchResult.destructured

            return HashCodeUriResponse(token = token)
        }
        return null
    }

    /**
     * Given an intent, this method tries to extract a JWT response from the intent data.
     * The expected format is a string of the form "access_token=<JWT>" appended to the `fragment`
     * part of the data URI
     * @param intent the intent received through a deep-link
     * @return a JWT token string if one could be extracted or `null` otherwise
     *
     * @throws IllegalArgumentException if the intent has no data
     *              or the action does not match [Intent.ACTION_VIEW]
     *              or the URI does not match the expected format
     * @throws RuntimeException if the data URI has an error block in the fragment part
     */
    fun extractTokenFromIntent(intent: Intent?): UriResponse {
        intent ?: throw IllegalArgumentException("Can't process a null intent")

        val appLinkData: Uri? = intent.data
                ?: throw IllegalArgumentException("Can't process an intent with no data")

        if (Intent.ACTION_VIEW == intent.action) {
            return extractTokenFromRedirectUri(appLinkData.toString())
        }
        else {
            throw IllegalArgumentException("Intent action has to be ${Intent.ACTION_VIEW}")
        }
    }
}

/**
 * Generic class for handling various response types
 **
 */
sealed class UriResponse


/**
 * Data Class to handle all JWT response types
 **
 */
data class JWTUriResponse(val token: String) : UriResponse()


/**
 * Data Class to handle all Transaction Hashcode response types
 **
 */
data class HashCodeUriResponse(val token: String) : UriResponse()


/**
 * Data Class to handle response errors
 **
 */
data class ErrorUriResponse(val message: String) : UriResponse()