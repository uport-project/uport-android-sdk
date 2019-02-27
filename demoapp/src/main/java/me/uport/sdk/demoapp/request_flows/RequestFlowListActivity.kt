package me.uport.sdk.demoapp.request_flows

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.simple_list.*
import me.uport.sdk.demoapp.R

/**
 *
 * Main request flow screen for the demo app
 * This lists all the request flows to be demonstrated within the app
 * Clicking on an item opens up a new activity for the specific flow
 *
 **/
class RequestFlowListActivity : AppCompatActivity() {

    private val features = arrayOf("Selective Disclosure", "Verified Claim Request", "Personal Signature Request", "Typed Data Signature Request", "Ethereum Transaction Request")

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
            1 -> startActivity(Intent(this, VerifiedClaimRequestActivity::class.java))
            2 -> startActivity(Intent(this, PersonalSignRequestActivity::class.java))
            3 -> startActivity(Intent(this, TypedDataRequestActivity::class.java))
            4 -> startActivity(Intent(this, EthereumTransactionActivity::class.java))
            else -> Toast.makeText(this, "Not Yet Implemented", Toast.LENGTH_LONG).show()
        }
    }
}