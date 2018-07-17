package me.uport.sdk.identity

import kotlin.coroutines.experimental.suspendCoroutine

typealias AccountCreatorCallback = (err: Exception?, acc: Account) -> Unit

interface AccountCreator {
    fun createAccount(networkId: String, forceRestart: Boolean = false, callback: AccountCreatorCallback)
}

suspend fun AccountCreator.createAccount(networkId: String, forceRestart: Boolean): Account = suspendCoroutine { continuation ->
    this.createAccount(networkId, forceRestart) { err, account ->
        if (err != null) {
            continuation.resumeWithException(err)
        } else {
            continuation.resume(account)
        }
    }
}