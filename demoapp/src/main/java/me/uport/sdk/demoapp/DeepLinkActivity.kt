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
        val text = try {
            val token = ResponseParser.extractTokenFromIntent(intent)
            "The response we got back from uPort is:\n$token"
        } catch (exception: RuntimeException) {
            "we got an error:\n${exception.message}"
        }
        deep_link_token.text = text
    }
}
