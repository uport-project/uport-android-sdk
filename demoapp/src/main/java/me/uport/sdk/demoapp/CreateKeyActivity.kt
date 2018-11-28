package me.uport.sdk.demoapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.encryption.KeyProtection
import kotlinx.android.synthetic.main.create_import_key.*
import me.uport.sdk.core.decodeBase64
import org.walleth.khex.toHexString

class CreateKeyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_import_key)

        create_key_btn.setOnClickListener {
            resetUI()
            UportHDSigner().createHDSeed(this, KeyProtection.Level.SIMPLE) { err, rootAddress, pubKey ->
                if (err == null) {
                    public_key_details.text = "publicKey: ${pubKey.decodeBase64().toHexString()}"
                    address_details.text = "address: $rootAddress"
                } else {
                    error_text.text = "error: $err"
                }
            }
        }
    }

    private fun resetUI() {
        public_key_details.text = ""
        address_details.text = ""
        error_text.text = ""
    }
}