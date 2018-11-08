package me.uport.sdk.demoapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.encryption.KeyProtection
import kotlinx.android.synthetic.main.create_import_key.*
import me.uport.sdk.core.decodeBase64
import org.walleth.khex.toHexString

class ImportKeyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_import_key)

        input_seed_phrase.visibility = View.VISIBLE
        create_key_btn.text = "Import Key"

        create_key_btn.setOnClickListener{

            val seedPhrase = input_seed_phrase.text.toString().trim()
            if (!seedPhrase.isEmpty()) {
                UportHDSigner().importHDSeed(this, KeyProtection.Level.SIMPLE, seedPhrase) {err, rootAddress, pubKey ->
                    if (err == null) {
                        public_key_details.text = "publicKey: ${pubKey.decodeBase64().toHexString()}"
                        address_details.text = "address: $rootAddress"
                    } else error_text.text = "error: ${err.toString()}"
                }
            } else {
                Toast.makeText(this, "Enter seed phrase", Toast.LENGTH_SHORT).show()
            }
        }
    }
}