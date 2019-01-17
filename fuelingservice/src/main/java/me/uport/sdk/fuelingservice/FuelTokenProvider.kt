package me.uport.sdk.fuelingservice

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.uport.sdk.core.IFuelTokenProvider
import me.uport.sdk.core.UI
import me.uport.sdk.core.urlPost
import org.json.JSONObject
import java.io.IOException

@Deprecated("This service is no longer supported. Use at your own risk")
class FuelTokenProvider(private val context: Context, private val dAppMnid: String) : IFuelTokenProvider {

    override fun onCreateFuelToken(deviceAddress: String, callback: (err: Exception?, fuelToken: String) -> Unit) {

        val ctx = context.applicationContext

        GlobalScope.launch {
            try {
                val uportInstanceToken = getInstanceToken(ctx, dAppMnid)
                val fuelToken = getFuelToken(deviceAddress, uportInstanceToken)

                withContext(UI) { callback(null, fuelToken) }
            } catch (ex: Exception) {
                //TODO: separate user solvable errors from fatal exceptions in documentation
                withContext(UI) { callback(ex, "") }
            }
        }
    }

    private suspend fun getFuelToken(deviceAddress: String, uportInstanceToken: String): String {

        val rawResponse = urlPost(FUELING_SERVICE_URL, "{\"iid_token\":\"$uportInstanceToken\", \"deviceAddress\":\"$deviceAddress\"}")

        val response = JSONObject(rawResponse)

        val status = response.optString("status", "fail")
        val fuelToken = response.optString("data", "")

        if (status == "success" && fuelToken.isNotEmpty()) {
            return fuelToken
        } else {
            val error = response.optJSONObject("error")?.optString("message", "unknown error")
            throw IOException("Can't resolve fuelToken: err=$error")
        }
    }

    private suspend fun getInstanceToken(context: Context, dAppMnid: String): String {

        val options = FirebaseOptions.Builder()
                .setApplicationId(FIREBASE_APPLICATION_ID)
//                .setApiKey()
                .build()

        val firebaseApp = try {
            FirebaseApp.getInstance(dAppMnid)
        } catch (ex: IllegalStateException) {
            FirebaseApp.initializeApp(context, options, dAppMnid)
        }

        val iidService = FirebaseInstanceId.getInstance(firebaseApp)

        return GlobalScope.async {
            iidService.getToken(FIREBASE_SENDER_ID, dAppMnid)
                    ?: "can't get uPort specific instance token"
        }.await()
    }

    companion object {
        //TODO: allow customization of these fields
        private const val FUELING_SERVICE_URL = "https://api.uport.space/nisaba/instance_fuel"
        private const val FIREBASE_SENDER_ID = "145443782008"
        private const val FIREBASE_APPLICATION_ID = "1:145443782008:android:fcbc466858327bf3"
    }
}