package me.uport.sdk.demoapp.managing_jwt

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.verify_jwt.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.uport.sdk.core.UI
import me.uport.sdk.demoapp.R
import me.uport.sdk.jwt.JWTTools

/**
 * Shows how to verify uport specific JWTs
 */
class VerifyJWTActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.verify_jwt)

        val jwtToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJjbGFpbXMiOnsibmFtZSI6IlIgRGFuZWVsIE9saXZhdyJ9LCJpYXQiOjE1NDgxNjM2ODgsImV4cCI6MjE3ODg4MzY4OCwiaXNzIjoiZGlkOmV0aHI6MHg0MTIzY2JkMTQzYjU1YzA2ZTQ1MWZmMjUzYWYwOTI4NmI2ODdhOTUwIn0.Tral9PIGcNIH-3LrC9sAasPokbtnny3LPw9wrEGPqARXLQREGH6l8GI9JXL3o6_qjY3KF9Nbz0wi7g-pdlC-rgA"

        jwtTokenView.text = jwtToken

        verify_btn.setOnClickListener {

            errorText.text = ""
            jwtPayload.text = ""
            progress.visibility = View.VISIBLE

            GlobalScope.launch {

                val payload = JWTTools().verify(jwtToken)

                withContext(UI) {
                    jwtPayload.text = payload.toString()
                    progress.visibility = View.INVISIBLE
                }
            }
        }
    }
}