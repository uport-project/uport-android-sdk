package me.uport.sdk.demoapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.encryption.KeyProtection
import kotlinx.android.synthetic.main.create_import_key.*
import me.uport.sdk.core.decodeBase64
import org.kethereum.bip39.generateMnemonic
import org.kethereum.bip39.wordlists.WORDLIST_ENGLISH
import org.walleth.khex.toHexString

class ImportKeyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_import_key)

        seed_phrase_layout.visibility = View.VISIBLE
        create_key_btn.text = "Import Key"

        generate_seed_phrase.setOnClickListener {
            val seedPhrase = generateMnemonic(wordList = WORDLIST_ENGLISH)
            input_seed_phrase.setText(seedPhrase)
        }

        create_key_btn.setOnClickListener {
            resetUI()
            val seedPhrase = input_seed_phrase.text.toString().trim()
            if (!seedPhrase.isEmpty()) {
                UportHDSigner().importHDSeed(this, KeyProtection.Level.SIMPLE, seedPhrase) { err, rootAddress, pubKey ->
                    if (err == null) {
                        public_key_details.text = "publicKey: ${pubKey.decodeBase64().toHexString()}"
                        address_details.text = "address: $rootAddress"
                    } else {
                        error_text.text = "error: $err"
                    }
                }
            } else {
                Toast.makeText(this, "Enter seed phrase", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetUI() {
        public_key_details.text = ""
        address_details.text = ""
        error_text.text = ""
    }
}