package me.uport.sdk.demoapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import me.uport.sdk.Uport
import me.uport.sdk.Uport.defaultAccount
import me.uport.sdk.core.Networks
import me.uport.sdk.demoapp.R.id.progressBar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Uport.defaultAccount == null) {
            progressBar.visibility = View.VISIBLE
            launch(UI) {
                try {
                    val acc = Uport.createAccount(Networks.rinkeby)
                } catch (ex: Exception) {
                    defaultAccount.text = "ERROR: $ex."
                }

                progressBar.visibility = View.INVISIBLE
            }
        } else {
            defaultAccount.text = Uport.defaultAccount?.toJson(true) ?: "creating account, please wait"
        }

    }
}
