package me.uport.sdk.demoapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

class DeepLinkActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deep_link)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val appLinkAction = intent.action
        val appLinkData: Uri? = intent.data
        if (Intent.ACTION_VIEW == appLinkAction) {
            appLinkData?.also { link ->
                Toast.makeText(this, link.toString(), Toast.LENGTH_LONG).show()
            } ?: Toast.makeText(this, "no data to parse in intent", Toast.LENGTH_SHORT).show()

//            appLinkData?.lastPathSegment?.also { recipeId ->
//                Uri.parse("https://uport-project.github.io/uport-android-sdk/")
//                        .buildUpon()
//                        .appendPath(recipeId)
//                        .build().also { appData ->
//                            showRecipe(appData)
//                        }
//            }
        }
    }
}
