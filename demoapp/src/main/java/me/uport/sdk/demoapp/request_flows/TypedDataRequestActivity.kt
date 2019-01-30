package me.uport.sdk.demoapp.request_flows

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.uport.sdk.signer.KPSigner
import kotlinx.android.synthetic.main.request_flow.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.uport.sdk.core.UI
import me.uport.sdk.demoapp.R
import me.uport.sdk.jwt.JWTTools
import me.uport.sdk.transport.Transports

/**
 *
 * This activity demonstrates the flow for creating and sending a [Typed Data Request]
 *
 **/

class TypedDataRequestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.request_flow)

        // instantiate signer
        val signer = KPSigner("0x1234")

        // create issuer DID
        val issuerDID = "did:ethr:${signer.getAddress()}"

        // create the request JWT payload

        val typedData = """
            {
                "types": {
                    "EIP712Domain": [{
                            "name": "name",
                            "type": "string"
                        },
                        {
                            "name": "version",
                            "type": "string"
                        }
                    ],
                    "Greeting": [{
                            "name": "text",
                            "type": "string"
                        }
                    ]
                },
                "domain": {
                    "name": "My dapp",
                    "version": "1.0",
                    "chainId": 1,
                    "verifyingContract": "0xdeadbeef",
                    "salt": "0x999999999910101010101010"
                },
                "primaryType": "Greeting",
                "message": {
                    "text": "Hello",
                    "subject": "World"
                }
            }""".trimIndent()


        val payload = mapOf<String, Any>(
                "callback" to "https://uport-project.github.io/uport-android-sdk",
                "type" to "eip712Req",
                "net" to "0x4",
                "iss" to issuerDID,
                "iat" to System.currentTimeMillis(),
                "typedData" to typedData
        )

        request_details.text = "" +
                "Request Type: Typed Data Request" +
                "\n" +
                "Issuer DID: $issuerDID" +
                "\n" +
                "Typed Data: ${payload["typedData"]}"

        // make request
        send_request.setOnClickListener {

            progress.visibility = View.VISIBLE

            GlobalScope.launch {
                val requestJWT = JWTTools().createJWT(payload, issuerDID, signer, 60 * 60)

                // Send a valid signed request to uport via Transports
                @Suppress
                Transports().send(this@TypedDataRequestActivity, requestJWT)

                withContext(UI) {
                    progress.visibility = View.GONE
                }
            }
        }

    }
}