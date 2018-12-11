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

        // val jwtToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJpYXQiOjE1MzUwMTY3MDIsImV4cCI6MTUzNTEwMzEwMiwiYXVkIjoiZGlkOmV0aHI6MHhhOWUzMjMyYjYxYmRiNjcyNzEyYjlhZTMzMTk1MDY5ZDhkNjUxYzFhIiwidHlwZSI6InNoYXJlUmVzcCIsIm5hZCI6IjJvZHpqVGFpOFJvNFYzS3hrbTNTblppdjlXU1l1Tm9aNEFoIiwib3duIjp7Im5hbWUiOiJ1UG9ydCBVc2VyIn0sInJlcSI6ImV5SjBlWEFpT2lKS1YxUWlMQ0poYkdjaU9pSkZVekkxTmtzdFVpSjkuZXlKcFlYUWlPakUxTXpVd01UWTJPREVzSW1WNGNDSTZNVFV6TlRBeE56STRNU3dpY21WeGRXVnpkR1ZrSWpwYkltNWhiV1VpTENKd2FHOXVaU0lzSW1OdmRXNTBjbmtpWFN3aWNHVnliV2x6YzJsdmJuTWlPbHNpYm05MGFXWnBZMkYwYVc5dWN5SmRMQ0pqWVd4c1ltRmpheUk2SW1oMGRIQnpPaTh2WTJoaGMzRjFhUzUxY0c5eWRDNXRaUzloY0drdmRqRXZkRzl3YVdNdmJVVXpTbVpXZWxOMFNuUnFhbnBvWWpSYVRFRnhkeUlzSW1GamRDSTZJbXRsZVhCaGFYSWlMQ0owZVhCbElqb2ljMmhoY21WU1pYRWlMQ0pwYzNNaU9pSmthV1E2WlhSb2Nqb3dlR0U1WlRNeU16SmlOakZpWkdJMk56STNNVEppT1dGbE16TXhPVFV3Tmpsa09HUTJOVEZqTVdFaWZRLnVScUdGd01XNnpWSDR4OWFmTDAtS29qSEYwVF9GbW9QWnR6OG5uSjRFXzhNY2cxejBBZ21aMnplOE5iS05wVUNnRHRwTU9RNzVGSjU4WmhzbWFxQUxBRSIsImNhcGFiaWxpdGllcyI6WyJleUowZVhBaU9pSktWMVFpTENKaGJHY2lPaUpGVXpJMU5rc3RVaUo5LmV5SnBZWFFpT2pFMU16VXdNVFkzTURFc0ltVjRjQ0k2TVRVek5qTXhNamN3TVN3aVlYVmtJam9pWkdsa09tVjBhSEk2TUhoaE9XVXpNak15WWpZeFltUmlOamN5TnpFeVlqbGhaVE16TVRrMU1EWTVaRGhrTmpVeFl6RmhJaXdpZEhsd1pTSTZJbTV2ZEdsbWFXTmhkR2x2Ym5NaUxDSjJZV3gxWlNJNkltRnlianBoZDNNNmMyNXpPblZ6TFhkbGMzUXRNam94TVRNeE9UWXlNVFkxTlRnNlpXNWtjRzlwYm5RdlIwTk5MM1ZRYjNKMEx6UXpNRGsxTWpZMkxUSmhPR1F0TTJFMFpTMWlaRFV3TFRka01USm1ZVE00TWpRNFlpSXNJbWx6Y3lJNkltUnBaRHBsZEdoeU9qQjRNVEE0TWpBNVpqUXlORGRpTjJabE5qWXdOV0l3WmpVNFpqa3hORFZsWXpNeU5qbGtNREUxTkNKOS5Lc0F6TmVDeHFDaF9rMkt4aTYtWHFveFNXZjBCLWFFR0xXdi1ldHVXQlF2QU5neDFTMG5oZ0ppRkllUnRXakw4ekdnVVV3MUlsSWJtYUZrOEo5aGdhd0UiXSwiYm94UHViIjoiY2g3aGI2S3hsakJ2bXh5UDJXZENWTFNTLzQ2S1hCcmdkWG1Mcm03VEpIST0iLCJpc3MiOiJkaWQ6ZXRocjoweDEwODIwOWY0MjQ3YjdmZTY2MDViMGY1OGY5MTQ1ZWMzMjY5ZDAxNTQifQ.Ncf8B_y0Ha8gdaYyCaL5jLX2RsKTMwxTQ8KlybXFygsxKUUQm9OXo4lU65fduIaFvVyPOP6Oe2adar8m0m2aiwA"

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