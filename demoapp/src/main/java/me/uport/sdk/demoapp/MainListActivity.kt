package me.uport.sdk.demoapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_list_main.*

/**
 *
 * Main screen for the demo app
 * This lists all the features to be demonstrated within the app
 * Clicking on an item opens up a new activity for the specific feature
 *
 **/

class MainListActivity : AppCompatActivity() {

    private val features = arrayOf("Create an Account", "Create a Key", "Import a Key", "Manage Keys","Create a JWT", "Verify a JWT", "Selective Disclosure")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_main)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, features)

        feature_list.adapter = adapter as ListAdapter?

        feature_list.setOnItemClickListener { _, _, position, _ ->
            itemSelected(position)
        }
    }

    private fun itemSelected(position: Int) {
        when (position) {
            0 -> startActivity(Intent(this, CreateAccountActivity::class.java))
            1 -> startActivity(Intent(this, CreateKeyActivity::class.java))
            2 -> startActivity(Intent(this, ImportKeyActivity::class.java))
            else -> Toast.makeText(this, "Not yet Implemented", Toast.LENGTH_LONG).show()
        }
    }
}