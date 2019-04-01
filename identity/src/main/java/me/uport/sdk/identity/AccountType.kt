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
    /**
    * This [Account] is backed by a key pair that is only present on this device.
    * This is deprecated, please use a [KeyPair]
    */
    @Deprecated("please use KeyPair instead")
    Device,
    IdentityManager,
}
