package me.uport.sdk.demoapp.request_flows


import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.request_flow.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.uport.sdk.signer.KPSigner
import me.uport.sdk.core.Networks
import me.uport.sdk.core.UI
import me.uport.sdk.credentials.Credentials
import me.uport.sdk.credentials.PersonalSignRequestParams
import me.uport.sdk.demoapp.R
import me.uport.sdk.jwt.JWTTools
import me.uport.sdk.transport.ErrorUriResponse
import me.uport.sdk.transport.IntentForwardingActivity
import me.uport.sdk.transport.JWTUriResponse
import me.uport.sdk.transport.ResponseParser
import me.uport.sdk.transport.Transports
import me.uport.sdk.transport.UriResponse

/**
 *
 * This activity demonstrates the flow for creating and sending a [Personal Signature Request]
 *
 **/
class PersonalSignRequestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.request_flow)

        // instantiate signer
        val signer = KPSigner("0x1234")

        // create issuer DID
        val issuerDID = "did:ethr:${signer.getAddress()}"

        // fetch the DID of the identity you want to sign the Data from intent
        val riss = intent.getStringExtra("riss")

        // create the request JWT
        val cred = Credentials(issuerDID, signer)
        val params = PersonalSignRequestParams(
                data = "This is a message I need you to sign",
                riss = riss,
                callbackUrl = "https://uport-project.github.io/uport-android-sdk/callbacks",
                networkId = Networks.rinkeby.networkId
        )

        request_details.text = "" +
                "Request Type: Personal Signature Request" +
                "\n" +
                "DID of the Identity to sign the data: $riss" +
                "\n" +
                "Data to be signed: ${params.data}"

        // make request
        send_request.setOnClickListener {

            progress.visibility = View.VISIBLE

            GlobalScope.launch {
                val requestJWT = cred.createPersonalSignRequest(params)

                // Send a valid signed request to uport via Transports
                @Suppress("LabeledExpression")
                Transports().sendExpectingResult(this@PersonalSignRequestActivity, requestJWT)

                withContext(UI) {
                    progress.visibility = View.GONE
                }
            }
        }
    }

    /**
     * The response sent via deeplink is dispatched back to this activity via [IntentForwardingActivity] when using [Transports.sendExpectingResult()]
     *
     * Parse the [UriResponse] and display the relevant message
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val response: UriResponse? = ResponseParser.parseActivityResult(requestCode, resultCode, data)

        when (response) {
            is JWTUriResponse -> {
                val (_, payloadMap, _) = JWTTools().decodeRaw(response.token)
                response_details.text = """
                        Signed Message : ${payloadMap["data"]}
                    """.trimIndent()
            }
            is ErrorUriResponse -> {
                response_details.text = "error: ${response.message}"
            }
            null -> {
                //process your other domain specific responses
            }
        }
    }
}
