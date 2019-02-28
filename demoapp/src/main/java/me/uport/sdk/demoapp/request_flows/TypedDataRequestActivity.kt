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
        @Suppress("StringLiteralDuplication")
        val payload = mapOf(
                "callback" to "https://uport-project.github.io/uport-android-sdk",
                "type" to "eip712Req",
                "net" to "0x4",
                "iss" to issuerDID,
                "iat" to System.currentTimeMillis(),
                "typedData" to mapOf(
                        "types" to mapOf(
                                "EIP712Domain" to listOf(
                                        mapOf(
                                                "name" to "name",
                                                "type" to "string"
                                        ),
                                        mapOf(
                                                "name" to "version",
                                                "type" to "string"
                                        ),
                                        mapOf(
                                                "name" to "chainId",
                                                "type" to "uint256"
                                        ),
                                        mapOf(
                                                "name" to "verifyingContract",
                                                "type" to "address"
                                        )),
                                "Person" to listOf(
                                        mapOf(
                                                "name" to "name",
                                                "type" to "string"
                                        ),
                                        mapOf(
                                                "name" to "wallet",
                                                "type" to "address"
                                        )),
                                "Mail" to listOf(
                                        mapOf(
                                                "name" to "from",
                                                "type" to "Person"
                                        ),
                                        mapOf(
                                                "name" to "to",
                                                "type" to "Person"
                                        ),
                                        mapOf(
                                                "name" to "contents",
                                                "type" to "string"
                                        ))

                        ),
                        "domain" to mapOf(
                                "name" to "Ether Mail",
                                "version" to "1",
                                "chainId" to "1",
                                "verifyingContract" to "0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC"
                        ),
                        "message" to mapOf(
                                "contents" to "Hello Bob",
                                "from" to mapOf(
                                        "name" to "Cow",
                                        "wallet" to "0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826"
                                ),
                                "to" to mapOf(
                                        "name" to "to",
                                        "wallet" to "0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB"
                                )),
                        "primaryType" to "Mail"
                )
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
                @Suppress("LabeledExpression")
                Transports().send(this@TypedDataRequestActivity, requestJWT)

                withContext(UI) {
                    progress.visibility = View.GONE
                }
            }
        }
    }
}