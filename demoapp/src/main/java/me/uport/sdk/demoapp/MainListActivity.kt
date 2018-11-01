package me.uport.sdk.demoapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.ListView

/**
 *
 * Main screen for the demo app
 * This lists all the features to be demonstrated within the app
 * Clicking on an item opens up a new activity for the specific feature
 *
 **/

class MainListActivity : AppCompatActivity() {

    val features = arrayOf("Create an Account", "Manage Keys", "Create a JWT", "Verify a JWT", "Selective Disclosure")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_main)

        var listView = findViewById<ListView>(R.id.feature_list)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, features)

        listView.adapter = adapter
    }
}