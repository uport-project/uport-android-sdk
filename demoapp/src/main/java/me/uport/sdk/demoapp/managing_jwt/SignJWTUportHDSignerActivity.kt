package me.uport.sdk.demoapp.managing_jwt

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.uport.sdk.signer.Signer
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.UportHDSignerImpl
import com.uport.sdk.signer.encryption.KeyProtection
import com.uport.sdk.signer.importHDSeed
import kotlinx.android.synthetic.main.simple_result_layout.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.uport.sdk.core.UI
import me.uport.sdk.demoapp.R
import me.uport.sdk.jwt.JWTTools

/**
 * shows how to create JWTs and sign them using keys managed by [UportHDSigner]
 */
class SignJWTUportHDSignerActivity : AppCompatActivity() {

    var issuerDID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.simple_result_layout)

        submit_btn_one.text = "Sign JWT"

        // mock a JWT payload
        val payload = mapOf<String, Any>(
                "claims" to mapOf(
                        "name" to "Steve Austin",
                        "dob" to "13 Aug 1976"
                )
        )

        /*
         * Ensure a seed is available.
         * For demonstration purposes this is done in the same process as signing the JWT
         * In actual use cases the seed would be created when the user is first on-boarded
         */

        val phrase = "notice suffer eagle " +
                "style exclude burst " +
                "write mechanic junior " +
                "crater crystal seek"

        var signer: Signer? = null
        GlobalScope.launch(UI) {
            val (address, _) = UportHDSigner().importHDSeed(this@SignJWTUportHDSignerActivity, KeyProtection.Level.PROMPT, phrase)

            // create KeyPair signer
            signer = UportHDSignerImpl(this@SignJWTUportHDSignerActivity, UportHDSigner(), address, address)
            issuerDID = "did:ethr:${signer?.getAddress()}"

            item_details_two.text = "Issuer DID: $issuerDID"
        }

        submit_btn_one.setOnClickListener {

            GlobalScope.launch(UI) {

                val signedJWT: String? = try {
                    JWTTools().createJWT(payload, issuerDID!!, signer!!,5000)
                } catch (exception: Exception) {
                    null
                }

                item_details_one.text = "Signed JWT Token: $signedJWT"

            }
        }
    }
}
