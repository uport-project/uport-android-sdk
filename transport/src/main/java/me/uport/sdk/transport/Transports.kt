package me.uport.sdk.transport

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

class Transports {

    /**
     *  Sends a selective disclosure request to the uPort app.
     *  If the uPort app is not present this will open a browser
     *
     * __API-maturity: new__
     *
     *  This method does not ensure any type of response.
     */
    fun send(context: Context, jwt: String) {
        val encodedQuery = URLEncoder.encode(jwt, "UTF-8")
        val uri = Uri.parse("https://id.uport.me/req/$encodedQuery?callback_type=redirect")


        //deprecated calling convention
//        val uri = Uri.parse("me.uport:me?requestToken=$encodedQuery")


        val intent = Intent(Intent.ACTION_VIEW, uri)
                .addCategory(Intent.CATEGORY_BROWSABLE)
        context.startActivity(intent)
    }

}