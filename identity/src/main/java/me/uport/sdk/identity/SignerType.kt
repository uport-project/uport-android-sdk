package me.uport.sdk.identity

import android.support.annotation.Keep

@Keep
enum class SignerType {
    KeyPair,
    MetaIdentityManager,
    Proxy,
    Device,
    IdentityManager,
}