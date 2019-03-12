package me.uport.sdk.transport

import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 * This activity is supposed to send requests and remain on top of the task stack to receive responses.
 * This is transparently added to the task stack when using [Transports.sendExpectingResult]
 * To be able to receive responses from this, add an [IntentForwardingActivity] declaration
 * to your app manifest with the relevant deep link intent filter(s)
 *
 * Deep link callbacks will be received by that [IntentForwardingActivity] and
 * grafted onto the original task stack so that you can use the [onActivityResult] callback
 */
class RequestDispatchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            //don't do anything on recreate
            return
        }
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent ?: return //nothing to handle

        when (intent.action) {
            ACTION_DISPATCH_REQUEST -> handleDispatch(intent)
            ACTION_VIEW -> handleURI(intent)
        }
    }

    private fun handleDispatch(intent: Intent) {
        intent.extras?.getString(EXTRA_REQUEST_URI, null)?.let {
            val uri = Uri.parse(it)
            val targetIntent = Intent(Intent.ACTION_VIEW, uri)
                    .addCategory(Intent.CATEGORY_BROWSABLE)
            startActivity(targetIntent)
        }
    }

    private fun handleURI(intent: Intent) {
        intent.data?.let {
            //this assumes that only relevant deeplinks are processed by this dispatcher
            val redirect = Intent().putExtra(EXTRA_REDIRECT_URI, it.toString())
            setResult(Activity.RESULT_OK, redirect)
            //this assumes some other activity is waiting for this result.
            //TODO: check if a result is expected
            finish()
        }
    }

    companion object {
        const val ACTION_DISPATCH_REQUEST = "me.uport.sdk.transport.action.dispatch_uri_request"

        const val EXTRA_REDIRECT_URI = "redirect_uri"
        const val EXTRA_REQUEST_URI = "req_uri"
    }

}