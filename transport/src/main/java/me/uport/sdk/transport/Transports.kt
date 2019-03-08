package me.uport.sdk.transport

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import me.uport.sdk.transport.RequestDispatchActivity.Companion.EXTRA_REQUEST_URI
import java.net.URLEncoder

/**
 * Convenience class used for interactions with the uPort app.
 */
class Transports {

    /**
     *  Sends a JWT request to the uPort app.
     *  If the uPort app is not present this will open a browser.
     *
     *  This method does not ensure any type of response.
     *  If the request JWT includes a callback url your app will have to
     *  declare an intent filter to handle it to be able to process responses.
     *
     *  **For the general case, it is much easier to use [sendExpectingResult]**
     *
     *  See [request flows](https://github.com/uport-project/specs#request-flows) in the specs.
     *
     *  See the [`credentials` module](https://github.com/uport-project/uport-android-sdk/tree/develop/credentials)
     *  for an easy API to build these requests
     *  and the [`demoapp`](https://github.com/uport-project/uport-android-sdk/tree/develop/demoapp) for more examples
     *
     * > __API volatility: high__
     */
    fun send(context: Context, jwt: String) {
        val uri = encodeURICall(jwt)

        val intent = Intent(Intent.ACTION_VIEW, uri)
                .addCategory(Intent.CATEGORY_BROWSABLE)

        context.startActivity(intent)
    }

    /**
     *  Sends a JWT request to the uPort app.
     *  If the uPort app is not present this will open a browser.
     *
     *   This method sets up a response channel so that deep links can be interpreted as
     *  `onActivityResult` by the [activity] that makes the requests.
     *
     *  To be able to use this pattern you have to include a callback url in you requests and
     *  declare an intent filter on a [IntentForwardingActivity] to handle it
     *
     *  Example:
     *
     *  Using a request with `https://example.com/callback` as callback url
     *  requires an intent filter like the example below:
     *
     *  ```xml
     *  <!-- you need to declare this activity name to be able to use `onActivityResult` -->
     *  <activity android:name="me.uport.sdk.transport.IntentForwardingActivity">
     *       <intent-filter>
     *           <action android:name="android.intent.action.VIEW" />
     *           <category android:name="android.intent.category.BROWSABLE" />
     *           <category android:name="android.intent.category.DEFAULT" />
     *
     *           <data
     *               android:scheme="https"
     *               android:host="example.com"
     *               android:path="/callback" />
     *       </intent-filter>
     *   </activity>
     *  ```
     *
     *  See [request flows](https://github.com/uport-project/specs#request-flows) in the specs.
     *
     *  See the [`credentials` module](https://github.com/uport-project/uport-android-sdk/tree/develop/credentials)
     *  for an easy API to build these requests
     *  and the [`demoapp`](https://github.com/uport-project/uport-android-sdk/tree/develop/demoapp) for more examples
     *
     * > __API volatility: high__
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
        const val UPORT_DEFAULT_REQUEST_CODE = 17727
    }
}