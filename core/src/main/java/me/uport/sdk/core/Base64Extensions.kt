package me.uport.sdk.core

import android.util.Base64

fun String.toBase64() = Base64.encodeToString(this.toByteArray(), Base64.NO_WRAP)
fun ByteArray.toBase64() = Base64.encodeToString(this, Base64.NO_WRAP)
fun String.toBase64UrlSafe() = Base64.encodeToString(this.toByteArray(), Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE)
fun ByteArray.toBase64UrlSafe() = Base64.encodeToString(this, Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE)

fun String.decodeBase64(): ByteArray = this
        //force url safe no padding so that it can be applied to all b64 formats
        .replace('+', '-')
        .replace('/', '_')
        .replace("=", "")
        .let { Base64.decode(it.toByteArray(), Base64.URL_SAFE or Base64.NO_PADDING) }
