package me.uport.sdk.demoapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.encryption.KeyProtection
import kotlinx.android.synthetic.main.simple_result_layout.*
import me.uport.sdk.core.decodeBase64
import org.kethereum.bip39.generateMnemonic
import org.kethereum.bip39.wordlists.WORDLIST_ENGLISH
import org.walleth.khex.toHexString

/**
 * Shows how to import a 12 word seed phrase and store tha seed under unauthenticated key protection
 */
class ImportKeyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.simple_result_layout)

        inner_simple_layout.visibility = View.VISIBLE
        submit_btn_one.text = "Import Key"

        submit_btn_two.setOnClickListener {
            val seedPhrase = generateMnemonic(wordList = WORDLIST_ENGLISH)
            text_input_area.setText(seedPhrase)
        }

        submit_btn_one.setOnClickListener {
            resetUI()
            val seedPhrase = text_input_area.text.toString().trim()
            if (!seedPhrase.isEmpty()) {
                UportHDSigner().importHDSeed(this, KeyProtection.Level.SIMPLE, seedPhrase) { err, rootAddress, pubKey ->
                    if (err == null) {
                        item_details_one.text = "publicKey: ${pubKey.decodeBase64().toHexString()}"
                        item_details_two.text = "address: $rootAddress"
                    } else {
                        error_details.text = formatException(err)
                    }
                }
            } else {
                Toast.makeText(this, "Enter seed phrase", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetUI() {
        item_details_one.text = ""
        item_details_two.text = ""
        error_details.text = ""
    }
}