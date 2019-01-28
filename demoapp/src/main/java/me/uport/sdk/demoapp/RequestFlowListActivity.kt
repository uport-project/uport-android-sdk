package me.uport.sdk.demoapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.simple_list.*

class RequestFlowListActivity : AppCompatActivity() {

    private val features = arrayOf("Selective Disclosure", "Verified Claim Request", "Personal Signature Request", "Send Verification Flow", "Ethereum Transaction Flow")

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
            0 -> startActivity(Intent(this, SelectiveDisclosureActivity::class.java))
            1 -> Toast.makeText(this, "Not yet implemented", Toast.LENGTH_LONG).show()
            2 -> Toast.makeText(this, "Not yet implemented", Toast.LENGTH_LONG).show()
            3 -> Toast.makeText(this, "Not yet implemented", Toast.LENGTH_LONG).show()
            4 -> Toast.makeText(this, "Not yet implemented", Toast.LENGTH_LONG).show()
            else -> Toast.makeText(this, "Not Yet Implemented", Toast.LENGTH_LONG).show()
        }
    }
}