package me.uport.sdk.demoapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.uport.sdk.signer.KPSigner
import kotlinx.android.synthetic.main.selective_disclosure.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.uport.sdk.credentials.Credentials
import me.uport.sdk.credentials.RequestAccountType
import me.uport.sdk.credentials.SelectiveDisclosureRequestParams
import me.uport.sdk.transport.Transports

class SelectiveDiscloureActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.selective_disclosure)


        val signer = KPSigner("0x1234")
        val issuerDID = "did:ethr:${signer.getAddress()}"

        val cred = Credentials(issuerDID, signer)

        // declare jwtPayload params
        val params = SelectiveDisclosureRequestParams(
                requested = listOf("name", "country"),
                callbackUrl = "myapp://get-back-to-me-with-response.url",
                verified = listOf("email"),
                networkId = "0x4",
                accountType = RequestAccountType.keypair,
                vc = emptyList(),
                expiresInSeconds = 1234L,
                extras = mapOf(
                        "hello" to "world",
                        "type" to "expect this to be overwritten"
                )
        )

        issuer_did.text = "Issuer DID $issuerDID"

        send_request.setOnClickListener {
            GlobalScope.launch {
                val signedJWT = cred.createDisclosureRequest(params)

                // Send a valid signed request to uport via Transports
                Transports().send(this@SelectiveDiscloureActivity, signedJWT)
            }
        }

    }
}