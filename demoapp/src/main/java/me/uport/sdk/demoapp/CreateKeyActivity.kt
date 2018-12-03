package me.uport.sdk.demoapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.encryption.KeyProtection
import kotlinx.android.synthetic.main.simple_result_layout.*
import me.uport.sdk.core.decodeBase64
import org.walleth.khex.toHexString

class CreateKeyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.simple_result_layout)

        submit_btn_one.setOnClickListener {
            resetUI()
            UportHDSigner().createHDSeed(this, KeyProtection.Level.SIMPLE) { err, rootAddress, pubKey ->
                if (err == null) {
                    item_details_one.text = "publicKey: ${pubKey.decodeBase64().toHexString()}"
                    item_details_two.text = "address: $rootAddress"
                } else {
                    error_details.text = "error: $err"
                }
            }
        }
    }

    private fun resetUI() {
        item_details_one.text = ""
        item_details_two.text = ""
        error_details.text = ""
    }
}