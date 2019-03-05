package me.uport.sdk.demoapp.request_flows

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.uport.sdk.signer.KPSigner
import kotlinx.android.synthetic.main.request_flow.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.uport.sdk.core.Networks
import me.uport.sdk.core.UI
import me.uport.sdk.credentials.Credentials
import me.uport.sdk.credentials.EthereumTransactionRequestParams
import me.uport.sdk.demoapp.R
import me.uport.sdk.transport.ResponseParser
import me.uport.sdk.transport.Transports
import me.uport.sdk.transport.UriResponse
import java.math.BigInteger

/**
 *
 * This activity demonstrates the flow for creating and sending a [Ethereum Transaction Request]
 *
 **/
class EthereumTransactionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.request_flow)

        // instantiate signer
        val signer = KPSigner("0x1234")

        // create issuer DID
        val issuerDID = "did:ethr:${signer.getAddress()}"

        // create the request JWT
        val cred = Credentials(issuerDID, signer)
        val params = EthereumTransactionRequestParams(
                to = signer.getAddress(),
                value = BigInteger("1"),
                callbackUrl = "https://uport-project.github.io/uport-android-sdk/callbacks",
                networkId = Networks.rinkeby.networkId
        )

        request_details.text = "" +
                "Request Type: Ethereum Transaction Request" +
                "\n" +
                "Issuer DID: $issuerDID" +
                "\n" +
                "Value of transaction: ${params.value}"

        // make request
        send_request.setOnClickListener {

            progress.visibility = View.VISIBLE

            GlobalScope.launch {
                val requestJWT = cred.createEthereumTransactionRequest(params)

                // Send a valid signed request to uport via Transports
                @Suppress("LabeledExpression")
                Transports().sendExpectingResult(this@EthereumTransactionActivity, requestJWT)

                withContext(UI) {
                    progress.visibility = View.GONE
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val response: UriResponse? = ResponseParser.parseActivityResult(requestCode, resultCode, data)

        when (response) {
            is UriResponse.HashCodeUriResponse -> {
                response_details.text = """
                Full Transaction Hash is:
                ${response.token}
                """.trimIndent()
            }
            is UriResponse.ErrorUriResponse -> {
                response_details.text = "error: ${response.message}"
            }
            null -> {
                //process your other domain specific responses
            }
        }
    }
}
