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
import me.uport.sdk.credentials.SelectiveDisclosureRequestParams
import me.uport.sdk.demoapp.R
import me.uport.sdk.transport.Transports

/**
 *
 * This activity demonstrates the flow for making creating and sending a [Selective Disclosure Request]
 * The response comes back to `https://uport-project.github.io/uport-android-sdk` deep link, which in the case of this demoapp is handled by `DeepLinkActivity.kt`
 **/

class SelectiveDisclosureActivity : AppCompatActivity() {

    var requestJWT = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.request_flow)

        // create a signer
        val signer = KPSigner("0x1234")

        // create a DID
        val issuerDID = "did:ethr:${signer.getAddress()}"

        // create the request JWT
        val cred = Credentials(issuerDID, signer)
        val params = SelectiveDisclosureRequestParams(
                requested = listOf("name"),
                callbackUrl = "https://uport-project.github.io/uport-android-sdk"
        )

        request_details.text = "" +
                "Request Type: Selective Disclosure" +
                "\n" +
                "Issuer DID: $issuerDID" +
                "\n" +
                "Requested: Name"

        // make the request
        send_request.setOnClickListener {

            progress.visibility = View.VISIBLE

            GlobalScope.launch {
                requestJWT = cred.createDisclosureRequest(params)

                // Send a valid signed request to uport via Transports
                @Suppress("LabeledExpression")
                Transports().send(this@SelectiveDisclosureActivity, requestJWT)

                withContext(UI) {
                    progress.visibility = View.GONE
                }
            }
        }

    }
}
