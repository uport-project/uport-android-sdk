package me.uport.sdk.jwt.model

import android.support.annotation.Keep
import com.squareup.moshi.Json

@Keep
class Claim(

        val key: String,

        val value: String
)