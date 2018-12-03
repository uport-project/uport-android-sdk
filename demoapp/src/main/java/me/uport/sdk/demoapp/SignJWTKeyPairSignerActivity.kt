package me.uport.sdk.demoapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.uport.sdk.signer.KPSigner
import kotlinx.android.synthetic.main.simple_result_layout.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.uport.sdk.core.UI
import me.uport.sdk.jwt.JWTTools

class SignJWTKeyPairSignerActivity : AppCompatActivity() {

    var signedJWT: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.simple_result_layout)

        submit_btn_one.text = "Sign JWT"

        // create a JWT payload
        val payload = mapOf<String, Any>(
                "claims" to mapOf(
                        "name" to "Steve Austin",
                        "dob" to "13 Aug 1976"
                )
        )

        // create KeyPair signer
        val signer = KPSigner("0x1234")
        val issuerDID = "did:ethr:${signer.getAddress()}"

        item_details_two.text = "Issuer DID: $issuerDID"

        submit_btn_one.setOnClickListener {

            // use coroutine function to create signed JWT and display results
            GlobalScope.launch (UI){
                signedJWT = try {
                    JWTTools().createJWT(payload, issuerDID, signer, 5000)
                } catch (exception: Exception){
                    null
                }

                item_details_one.text = "Signed JWT Token: $signedJWT"
            }
        }
    }
}