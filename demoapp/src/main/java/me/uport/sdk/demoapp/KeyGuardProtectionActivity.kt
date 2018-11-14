package me.uport.sdk.demoapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.encryption.KeyProtection
import kotlinx.android.synthetic.main.key_protection_activity.*
import kotlinx.android.synthetic.main.create_import_key.*
import kotlinx.android.synthetic.main.key_protection_activity.*
import me.uport.sdk.core.decodeBase64
import org.walleth.khex.toHexString


class KeyGuardProtectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.key_protection_activity)

        var errorMessage = ""

        UportHDSigner().hasSecuredKeyguard(this) {
            errorMessage += "\nSecured Keyguard: $it"
            error.text = errorMessage
        }

        create_key.setOnClickListener {

            UportHDSigner().createHDSeed(this, KeyProtection.Level.SINGLE_PROMPT) { err, rootAddress, pubKey ->
                if (err == null) {
                    key_details.text = "publicKey: ${pubKey.decodeBase64().toHexString()}"
                    //address_details.text = "address: $rootAddress"
                } else error.text = "error: ${err.toString()}"
            }
        }
    }
}
