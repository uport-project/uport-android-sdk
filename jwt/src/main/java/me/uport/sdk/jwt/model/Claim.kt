package me.uport.sdk.jwt.model

import android.support.annotation.Keep

@Keep
class Claim(

        val key: String,

        val value: String
)