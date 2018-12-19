package me.uport.sdk.transport

import android.content.Intent
import android.net.Uri
import java.net.URI

/**
 * This class should be used when receiving a deeplink callback
 * It contains helpers for interpreting responses
 */
object ResponseParser {

    //language=RegExp
    private val fragmentMatcher = ".*[&#]*(access_token=([A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-]+))&*.*$".toRegex()

    /**
     * Given a deep link uri, this method tries to extract a JWT response from it.
     * @param redirectUrl the deeplink URI
     * @return a JWT token string if one could be extracted or `null` otherwise
     */
    fun extractTokenFromRedirectUri(redirectUrl: String): String? {
        val uriFragment = try {
            URI.create(redirectUrl).fragment
        } catch (ex: Exception) {
            null
        } ?: return null

        val matchResult = fragmentMatcher.matchEntire(uriFragment) ?: return null
        val (_, token) = matchResult.destructured
        return token
    }

    /**
     * Given an intent, this method tries to extract a JWT response from the intent data.
     * @param intent the intent received through a deep-link
     * @return a JWT token string if one could be extracted or `null` otherwise
     */
    fun extractTokenFromIntent(intent: Intent?): String? {
        intent ?: return null

        val appLinkAction = intent.action ?: return null
        val appLinkData: Uri? = intent.data ?: return null
        if (appLinkAction == Intent.ACTION_VIEW) {
            return extractTokenFromRedirectUri(appLinkData.toString())
        }
        return null
    }

}
