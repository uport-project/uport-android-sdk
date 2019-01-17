package me.uport.sdk.core

import okhttp3.*
import java.io.IOException

val okClient by lazy { OkHttpClient() }

/**
 * HTTP posts a [jsonBody] to the given [url] and calls back with the response body as string or an exception
 * Takes an optional [authToken] that will be sent as `Bearer` token on an `Authorization` header
 *
 * Calls back with [IOException] if the request could not be executed due to cancellation, disconnect or timeout
 */
fun urlPost(url: String, jsonBody: String, authToken: String? = null, callback: (err: Exception?, payload: String) -> Unit) {
    val contentType = MediaType.parse("application/json")

    val body = RequestBody.create(contentType, jsonBody)
    val request = Request.Builder().apply {
        url(url)
        addHeader("Accept", "application/json")
        addHeader("Content-Type", "application/json")
        if (authToken != null) {
            addHeader("Authorization", "Bearer $authToken")
        }
        post(body)
    }.build()

    okClient.newCall(request).enqueue(object : Callback {
        override fun onResponse(call: Call?, response: Response?) {
            if (response?.isSuccessful == true) {
                val payload = response.body()?.use {
                    it.string()
                } ?: ""
                callback(null, payload)
            } else {
                val code = response?.code()
                callback(IOException("server responded with HTTP $code"), "")
            }
        }

        override fun onFailure(call: Call?, err: IOException?) {
            callback(err, "")
        }
    })
}

/**
 * Does a HTTP GET with given [url] and calls back with the response body as string or an exception
 * Takes an optional [authToken] that will be sent as `Bearer` token on an `Authorization` header
 *
 * Calls back with [IOException] if the request could not be executed due to cancellation, disconnect or timeout.
 */
fun urlGet(url: String, authToken: String? = null, callback: (err: Exception?, payload: String) -> Unit) {
    val request = Request.Builder().apply {
        url(url)
        addHeader("Accept", "application/json")
        if (authToken != null) {
            addHeader("Authorization", "Bearer $authToken")
        }
    }.build()

    okClient.newCall(request).enqueue(object : Callback {
        override fun onResponse(call: Call?, response: Response?) {
            if (response?.isSuccessful == true) {
                val payload = response.body()?.use {
                    it.string()
                } ?: ""
                callback(null, payload)
            } else {
                val code = response?.code()
                callback(IOException("server responded with HTTP $code"), "")
            }
        }

        override fun onFailure(call: Call?, err: IOException?) {
            callback(err, "")
        }
    })
}