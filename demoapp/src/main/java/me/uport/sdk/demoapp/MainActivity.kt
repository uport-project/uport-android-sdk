package me.uport.sdk.demoapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import me.uport.sdk.Uport
import me.uport.sdk.core.Networks
import kotlinx.android.synthetic.main.activity_main.*
import me.uport.sdk.extensions.send


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Uport.defaultAccount == null) {
            defaultAccountView.text = "creating account, please wait"
            progressBarView.visibility = View.VISIBLE

            Uport.createAccount(Networks.rinkeby) { err, acc ->
                progressBarView.visibility = View.INVISIBLE

                if (err == null) {
                    defaultAccountView.text = acc.toJson(true)
                } else {
                    defaultAccountView.text = "ERROR: $err."
                }
            }
        } else {
            defaultAccountView.text = Uport.defaultAccount?.toJson(true)
        }

    }
}
