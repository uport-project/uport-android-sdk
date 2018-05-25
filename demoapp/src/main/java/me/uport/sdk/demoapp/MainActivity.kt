package me.uport.sdk.demoapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import me.uport.sdk.Uport
import me.uport.sdk.core.Networks

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar.visibility = View.VISIBLE
        defaultAccount.text = Uport.defaultAccount?.toJson(true) ?: "creating account, please wait"

        Uport.createAccount(Networks.rinkeby) { err, acc ->
            progressBar.visibility = View.INVISIBLE

            if (err == null) {
                defaultAccount.text = acc.toJson(true) ?: "null"
            } else {
                defaultAccount.text = "ERROR: $err."
            }
        }
    }
}
