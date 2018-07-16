package me.uport.sdk.identity

interface AccountCreator {
    fun createAccount(networkId: String, forceRestart: Boolean = false, callback: AccountCreatorCallback)
}