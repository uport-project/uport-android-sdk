package me.uport.sdk.demoapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_deep_link.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.uport.sdk.core.UI
import me.uport.sdk.credentials.JWTType
import me.uport.sdk.jwt.JWTTools
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
        GlobalScope.launch(UI) {
            val text = try {
                println("got called with ${intent?.data}")
                val token = ResponseParser.extractTokenFromIntent(intent) ?: ""
                val payload = withContext(Dispatchers.IO) { JWTTools().verify(token) }
                val (_, payloadRaw, _) = JWTTools().decodeRaw(token)

                val knownType = JWTType.valueOf(payload.type ?: JWTType.verResp.name)

                val response = when (knownType) {
                    JWTType.shareResp -> "name=${payload.own?.get("name")}"
                    JWTType.personalSignResp -> "message was signed: '${payloadRaw["data"]}'"
                    JWTType.eip712Resp -> "signature=${payloadRaw["signature"]}"
                    JWTType.verResp -> "signed claim=${payloadRaw["claim"]}"
                    else -> "unknown response type"
                }
                """
                The response we got is:
                $response


                Full JWT response is:
                $payloadRaw
                """.trimIndent()

            } catch (exception: RuntimeException) {
                "we got an error:\n${exception.message}"
            }
            deep_link_token.text = text
        }
    }
}
