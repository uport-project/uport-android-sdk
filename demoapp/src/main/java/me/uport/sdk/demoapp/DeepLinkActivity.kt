package me.uport.sdk.demoapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_deep_link.*
import me.uport.sdk.transport.ResponseParser

class DeepLinkActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deep_link)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val token = ResponseParser.extractTokenFromIntent(intent)
        val text = if (token == null) {
            "nothing can be extracted from the intent:\n${intent?.data}"
        } else {
            "The response we got back from uPort is:\n$token"
        }
        deep_link_token.text = text
    }
}
