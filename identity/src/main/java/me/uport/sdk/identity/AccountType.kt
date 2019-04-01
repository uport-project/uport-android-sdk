package me.uport.sdk.identity

import android.support.annotation.Keep

@Keep
enum class AccountType {
    /**
    * This [Account] is backed by a key pair that resides in memory.
    */
    KeyPair,
    HDKeyPair,
    MetaIdentityManager,
    Proxy,
    Device,
    IdentityManager,
}
