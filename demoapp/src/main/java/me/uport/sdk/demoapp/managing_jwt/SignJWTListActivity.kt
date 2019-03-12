package me.uport.sdk.demoapp.managing_jwt

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.simple_list.*
import me.uport.sdk.demoapp.R

/**
 * entry screen for JWT signing demos
 */
class SignJWTListActivity : AppCompatActivity() {

    private val features = arrayOf("Use KeyPair Signer", "Use UportHDSigner with Fingerprint")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.simple_list)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, features)

        item_list.adapter = adapter

        item_list.setOnItemClickListener { _, _, position, _ ->
            itemSelected(position)
        }
    }

    private fun itemSelected(position: Int) {
        when (position) {
            0 -> startActivity(Intent(this, SignJWTKeyPairSignerActivity::class.java))
            1 -> startActivity(Intent(this, SignJWTUportHDSignerActivity::class.java))
        }
    }
}