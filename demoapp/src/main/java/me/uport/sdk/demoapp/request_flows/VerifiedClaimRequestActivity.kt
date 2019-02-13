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
import me.uport.sdk.credentials.Credentials
import me.uport.sdk.credentials.VerifiedClaimRequestParams
import me.uport.sdk.demoapp.R
import me.uport.sdk.transport.Transports

/**
 *
 * This activity demonstrates the flow for creating and sending a [Verified Claim Request]
 *
 **/
class VerifiedClaimRequestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.request_flow)

        // instantiate signer
        val signer = KPSigner("0x1234")

        // create issuer DID
        val issuerDID = "did:ethr:${signer.getAddress()}"

        // create the request JWT payload
        val params = VerifiedClaimRequestParams(
                unsignedClaim = mapOf("citizen of Cleverland" to true),
                sub = issuerDID,
                callbackUrl = "https://uport-project.github.io/uport-android-sdk"
        )

        request_details.text = "" +
                "Request Type: Verification" +
                "\n" +
                "Issuer DID: $issuerDID" +
                "\n" +
                "Unsigned Claim: ${params.unsignedClaim}"

        // make request
        send_request.setOnClickListener {

            progress.visibility = View.VISIBLE

            GlobalScope.launch {
                val requestJWT = Credentials(issuerDID, signer).createVerificationSignatureRequest(params)

                // Send a valid signed request to uport via Transports
                @Suppress("LabeledExpression")
                Transports().send(this@VerifiedClaimRequestActivity, requestJWT)

                withContext(UI) {
                    progress.visibility = View.GONE
                }
            }
        }

    }
}