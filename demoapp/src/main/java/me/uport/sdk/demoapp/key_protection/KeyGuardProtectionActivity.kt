package me.uport.sdk.demoapp.key_protection

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.encryption.KeyProtection
import kotlinx.android.synthetic.main.key_protection_activity.*
import me.uport.sdk.core.decodeBase64
import me.uport.sdk.core.padBase64
import me.uport.sdk.core.toBase64
import me.uport.sdk.demoapp.R
import me.uport.sdk.demoapp.formatException
import org.walleth.khex.toHexString
import java.util.*

/**
 * Shows how to create an HD seed and store it under a keystore authenticated by device keyguard.
 */
class KeyGuardProtectionActivity : AppCompatActivity() {

    private var address: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.key_protection_activity)

        var errorMessage = ""

        // checks if the user has setup any keyguard on the device
        UportHDSigner().hasSecuredKeyguard(this) {
            errorMessage += "\nSecured Keyguard: $it"
            error.text = errorMessage
        }

        create_key.setOnClickListener {

            // creating a seed using single prompt protection level which prompts the user for a keycode
            UportHDSigner().createHDSeed(this, KeyProtection.Level.SINGLE_PROMPT) { err, rootAddress, pubKey ->
                if (err == null) {
                    key_details.text = "publicKey: ${pubKey.decodeBase64().toHexString()}"
                    address = rootAddress
                } else {
                    error.text = formatException(err)
                }
            }
        }

        sign_btn.setOnClickListener {

            val msgBytes = ByteArray(3139).also { resultArray -> Random().nextBytes(resultArray) }

            val payload = msgBytes.toBase64().padBase64()

            // creating a seed using single prompt protection level which prompts the user for a keycode
            UportHDSigner().signTransaction(this, address, "m/94'/62'/0'/0/0", payload, "${getString(R.string.app_name)} is requesting your approval to sign a string with a newly minted key") { err, sig ->
                if (err == null) {
                    signed_string_details.text = "Signed Successfully : $sig"
                } else {
                    error.text = formatException(err)
                }
            }
        }
    }
}
