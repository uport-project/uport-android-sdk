package me.uport.sdk.jwt.model

import android.support.annotation.Keep
import com.squareup.moshi.Json

@Keep
class JwtHeader(
        /**
         * Standard JWT headeer
         */
        val typ: String,

        val alg: String
)