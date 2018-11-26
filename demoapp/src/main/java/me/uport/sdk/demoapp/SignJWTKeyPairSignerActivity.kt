package me.uport.sdk.demoapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.uport.sdk.signer.KPSigner
import kotlinx.android.synthetic.main.create_import_key.*
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import me.uport.sdk.core.UI
import me.uport.sdk.jwt.JWTTools

class SignJWTKeyPairSignerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_import_key)

        create_key_btn.text = "Sign JWT"

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


        create_key_btn.setOnClickListener {

            // use coroutine function to create signed JWT and display results
            GlobalScope.launch (UI){
                val signedJWT: String? = try {
                    JWTTools().createJWT(payload, issuerDID, signer, 5000)
                } catch (exception: Exception){
                    null
                }

                public_key_details.text = "Signed JWT Token: ${signedJWT}"

                address_details.text = "Issuer DID: ${issuerDID}"
            }
        }
    }
}