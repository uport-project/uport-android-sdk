package me.uport.sdk.demoapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.simple_list.*

/**
 *
 * Main screen for the demo app
 * This lists all the features to be demonstrated within the app
 * Clicking on an item opens up a new activity for the specific feature
 *
 **/

class MainListActivity : AppCompatActivity() {

    private val features = arrayOf("Create an Account", "Create a Key", "Import a Key", "Key Protection","Create a Signed JWT", "Selective Disclosure")

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
            0 -> startActivity(Intent(this, CreateAccountActivity::class.java))
            1 -> startActivity(Intent(this, CreateKeyActivity::class.java))
            2 -> startActivity(Intent(this, ImportKeyActivity::class.java))
            3 -> startActivity(Intent(this, KeyProtectionListActivity::class.java))
            4 -> startActivity(Intent(this, SignJWTListActivity::class.java))
            else -> Toast.makeText(this, "Not Yet Implemented", Toast.LENGTH_LONG).show()
        }
    }
}