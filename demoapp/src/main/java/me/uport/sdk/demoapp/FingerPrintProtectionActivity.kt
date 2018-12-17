package me.uport.sdk.demoapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.encryption.KeyProtection
import kotlinx.android.synthetic.main.key_protection_activity.*
import me.uport.sdk.core.decodeBase64
import me.uport.sdk.core.padBase64
import me.uport.sdk.core.toBase64
import org.walleth.khex.toHexString
import java.util.*


class FingerPrintProtectionActivity : AppCompatActivity() {

    private var address: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.key_protection_activity)

        var errorMessage = ""

        // checks to see if device has the required fingerprint hardware
        UportHDSigner().hasFingerprintHardware(this) {
            errorMessage += "\nFingerPrint Hardware: $it"
            error.text = errorMessage
        }

        // checks to see if the user has setup fingerprint authentication
        UportHDSigner().hasSetupFingerprints(this) {
            errorMessage += "\nFingerPrint Setup: $it"
            error.text = errorMessage
        }

        /**
         *
         * Note: If none of the above are true the system will default to using single prompt protection level
         *
         **/

        create_key.setOnClickListener {
            // creating a seed using prompt protection level which prompts the user for fingerprint authentication
            UportHDSigner().createHDSeed(this, KeyProtection.Level.PROMPT) { err, rootAddress, pubKey ->
                if (err == null) {
                    key_details.text = "publicKey: ${pubKey.decodeBase64().toHexString()}"
                    address = rootAddress
                } else {
                    error.text = "error: $err"
                }
            }
        }

        sign_btn.setOnClickListener {

            val msgBytes = ByteArray(3139).also { resultArray -> Random().nextBytes(resultArray) }

            val payload = msgBytes.toBase64().padBase64()

            UportHDSigner().signTransaction(this, address, "m/94'/62'/0'/0/0", payload, "${getString(R.string.app_name)} is requesting your approval to sign a string with a newly minted key") { err, sig ->
                if (err == null) {
                    signed_string_details.text = "Signed Successfully : $sig"
                } else {
                    error.text = "error: $err"
                }
            }
        }
    }
}
