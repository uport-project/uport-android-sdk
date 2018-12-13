package me.uport.sdk.demoapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.verify_jwt.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.uport.sdk.core.UI
import me.uport.sdk.jwt.JWTTools

class VerifyJWTActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.verify_jwt)

        val jwtToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NksifQ.eyJpc3MiOiIyb2VYdWZIR0RwVTUxYmZLQnNaRGR1N0plOXdlSjNyN3NWRyIsImlhdCI6MTUyMDM2NjQzMiwicmVxdWVzdGVkIjpbIm5hbWUiLCJwaG9uZSIsImNvdW50cnkiLCJhdmF0YXIiXSwicGVybWlzc2lvbnMiOlsibm90aWZpY2F0aW9ucyJdLCJjYWxsYmFjayI6Imh0dHBzOi8vY2hhc3F1aS51cG9ydC5tZS9hcGkvdjEvdG9waWMvWG5IZnlldjUxeHNka0R0dSIsIm5ldCI6IjB4NCIsImV4cCI6MTUyMDM2NzAzMiwidHlwZSI6InNoYXJlUmVxIn0.C8mPCCtWlYAnroduqysXYRl5xvrOdx1r4iq3A3SmGDGZu47UGTnjiZCOrOQ8A5lZ0M9JfDpZDETCKGdJ7KUeWQ"

        jwtTokenView.text = jwtToken

        verify_btn.setOnClickListener {

            errorText.text = ""
            jwtPayload.text = ""
            progress.visibility = View.VISIBLE

            GlobalScope.launch {

                val payload = JWTTools().verify(jwtToken)

                withContext(UI) {
                    jwtPayload.text = payload?.toString()
                    progress.visibility = View.INVISIBLE
                }
            }
        }
    }
}