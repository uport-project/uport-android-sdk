package me.uport.sdk.transport

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import me.uport.sdk.transport.RequestDispatchActivity.Companion.EXTRA_REQUEST_URI
import java.net.URLEncoder

class Transports {

    /**
     *  Sends a selective disclosure request to the uPort app.
     *  If the uPort app is not present this will open a browser
     *
     * __API volatility: high__
     *
     *  This method does not ensure any type of response.
     */
    fun send(context: Context, jwt: String) {
        val uri = encodeURICall(jwt)

        val intent = Intent(Intent.ACTION_VIEW, uri)
                .addCategory(Intent.CATEGORY_BROWSABLE)

        context.startActivity(intent)
    }

    /**
     *  Sends a selective disclosure request to the uPort app.
     *  If the uPort app is not present this will open a browser
     *
     * __API volatility: high__
     *
     *  This method sets up a response channel so that deep links can be interpreted as
     *  `onActivityResult` by the [activity] that makes the requests.
     */
    fun sendExpectingResult(activity: Activity, jwt: String, requestCode: Int = UPORT_DEFAULT_REQUEST_CODE) {
        val uri = encodeURICall(jwt)

        val dispatchIntent = Intent(activity, RequestDispatchActivity::class.java)
                .setAction(RequestDispatchActivity.ACTION_DISPATCH_REQUEST)
                .putExtra(EXTRA_REQUEST_URI, uri.toString())

        activity.startActivityForResult(dispatchIntent, requestCode)
    }

    private fun encodeURICall(jwt: String): Uri {
        val encodedQuery = URLEncoder.encode(jwt, "UTF-8")
        return Uri.parse("https://id.uport.me/req/$encodedQuery?callback_type=redirect")
    }

    companion object {
        const val UPORT_DEFAULT_REQUEST_CODE = 47852365
    }
}