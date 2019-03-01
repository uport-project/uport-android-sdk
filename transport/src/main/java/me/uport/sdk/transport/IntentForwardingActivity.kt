package me.uport.sdk.transport

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 * This activity is supposed to interpret deep link responses and forward them
 * to the proper activity stack so that they can be presented as `onActivityResult`
 */
class IntentForwardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            ACTION_VIEW -> handleURI(intent)
        }
    }

    private fun handleURI(intent: Intent) {
        intent.data?.let {

            val redirectIntent = intent
                    .setClass(applicationContext, RequestDispatchActivity::class.java)
                    .setFlags(FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_NEW_TASK)
            //grafting the received intent onto the original activity stack
            startActivity(redirectIntent)
            finish()
        }
    }

}