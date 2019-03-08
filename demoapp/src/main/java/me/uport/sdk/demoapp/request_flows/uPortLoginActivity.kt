package me.uport.sdk.demoapp.request_flows

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.uport.sdk.signer.KPSigner
import kotlinx.android.synthetic.main.activity_uport_login.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.uport.sdk.core.UI
import me.uport.sdk.credentials.Credentials
import me.uport.sdk.credentials.SelectiveDisclosureRequestParams
import me.uport.sdk.demoapp.R
import me.uport.sdk.jwt.JWTTools
import me.uport.sdk.transport.ErrorUriResponse
import me.uport.sdk.transport.JWTUriResponse
import me.uport.sdk.transport.ResponseParser
import me.uport.sdk.transport.Transports
import me.uport.sdk.transport.UriResponse

/**
 * This allows the users initiate a uPort login using [SelectiveDisclosureRequest]
 * and then receive the deeplink response via [onActivityResult]
 */
class uPortLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_uport_login)

        // create a signer
        val signer = KPSigner("0x1234")

        // create a DID
        val issuerDID = "did:ethr:${signer.getAddress()}"

        // create the request JWT
        val cred = Credentials(issuerDID, signer)
        val params = SelectiveDisclosureRequestParams(
                requested = listOf("name"),
                callbackUrl = "https://uport-project.github.io/uport-android-sdk/callbacks"
        )

        request_details.text = """
            Selective disclosure
            Requesting `name` (and implicit DID) from uPort app user.
        """.trimIndent()

        // make the request
        btn_send_request.setOnClickListener {

            progress.visibility = View.VISIBLE

            GlobalScope.launch {
                val requestJWT = cred.createDisclosureRequest(params)

                // Send a signed request to uport via Transports
                @Suppress("LabeledExpression")
                Transports().sendExpectingResult(this@uPortLoginActivity, requestJWT)

                withContext(UI) {
                    progress.visibility = View.GONE
                }
            }
        }
    }

    @Suppress("UnsafeCast")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val response: UriResponse? = ResponseParser.parseActivityResult(requestCode, resultCode, data)

        when (response) {
            is JWTUriResponse -> {
                val (_, payloadMap, _) = JWTTools().decodeRaw(response.token)
                response_details.text = """
                        profile: ${payloadMap["own"]}
                        uPort app user DID: ${payloadMap["iss"]}
                    """.trimIndent()

                createRequestFlowOptions((payloadMap["iss"] as String))
            }
            is ErrorUriResponse -> {
                response_details.text = "error: ${response.message}"
            }
            null -> {
                //process your other domain specific responses
            }
        }
    }

    private fun createRequestFlowOptions(iss: String) {

        btn_verified_claim.visibility = View.VISIBLE
        btn_verified_claim.setOnClickListener {
            val intent = Intent(this, VerifiedClaimRequestActivity::class.java)
            intent.putExtra("iss", iss)
            startActivity(intent)
        }

        btn_personal_signature.visibility = View.VISIBLE
        btn_personal_signature.setOnClickListener {
            val intent = Intent(this, PersonalSignRequestActivity::class.java)
            intent.putExtra("iss", iss)
            startActivity(intent)
        }

        btn_typed_data.visibility = View.VISIBLE
        btn_typed_data.setOnClickListener {
            val intent = Intent(this, TypedDataRequestActivity::class.java)
            intent.putExtra("iss", iss)
            startActivity(intent)
        }

        btn_ethereum_transaction.visibility = View.VISIBLE
        btn_ethereum_transaction.setOnClickListener {
            val intent = Intent(this, EthereumTransactionActivity::class.java)
            intent.putExtra("iss", iss)
            startActivity(intent)
        }
    }
}
