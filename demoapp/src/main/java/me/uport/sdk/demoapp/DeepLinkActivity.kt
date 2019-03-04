package me.uport.sdk.demoapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_deep_link.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.uport.sdk.core.UI
import me.uport.sdk.credentials.JWTTypes
import me.uport.sdk.jwt.JWTTools
import me.uport.sdk.transport.ResponseParser
import me.uport.sdk.transport.UriResponse
import me.uport.sdk.transport.UriResponse.ErrorUriResponse
import me.uport.sdk.transport.UriResponse.HashCodeUriResponse
import me.uport.sdk.transport.UriResponse.JWTUriResponse

/**
 * Handles JWT and TX responses coming in as deep links
 *
 * The deep links are set as callback URLs in various request_flows
 */
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

        println("got called with ${intent?.data}")

        val uriResponse: UriResponse = ResponseParser.extractTokenFromIntent(intent)

        val text = when (uriResponse) {
            is JWTUriResponse -> processJWTResponse(uriResponse.token)
            is ErrorUriResponse -> processErrorResponse(uriResponse.message)
            is HashCodeUriResponse -> processHashcodeResponse(uriResponse.token)
        }

        deep_link_token.text = text
    }

    /**
     * Process the [HashCodeUriResponse] to human readable message
     */
    private fun processHashcodeResponse(token: String): String {
        return """
                Full Transaction Hash is:
                $token
                """.trimIndent()
    }

    /**
     * Process the [ErrorUriResponse] to human readable message
     */
    private fun processErrorResponse(message: String): String {
        return """
                We got an error:
                $message
                """.trimIndent()
    }

    /**
     * Process the [JWTUriResponse] to human readable message
     */
    private fun processJWTResponse(token: String): String = runBlocking(UI) {
        val payload = withContext(Dispatchers.IO) { JWTTools().verify(token) }
        val (_, payloadRaw, _) = JWTTools().decodeRaw(token)

        val knownType = JWTTypes.valueOf(payload.type ?: JWTTypes.verResp.name)

        val response = when (knownType) {
            JWTTypes.shareResp -> "name=${payload.own?.get("name")}"
            JWTTypes.personalSignResp -> "message was signed: '${payloadRaw["data"]}'"
            JWTTypes.eip712Resp -> "signature=${payloadRaw["signature"]}"
            JWTTypes.verResp -> "signed claim=${payloadRaw["claim"]}"
            else -> "unknown response type"
        }

        """
                The response we got is:
                $response


                Full JWT response is:
                $payloadRaw
                """.trimIndent()
    }
}
