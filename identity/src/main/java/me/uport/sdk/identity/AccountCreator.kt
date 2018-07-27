package me.uport.sdk.identity

import kotlin.coroutines.experimental.suspendCoroutine

typealias AccountCreatorCallback = (err: Exception?, acc: Account) -> Unit

interface AccountCreator {
    fun createAccount(networkId: String, forceRestart: Boolean = false, callback: AccountCreatorCallback)

    fun importAccount(networkId: String, seedPhrase: String, forceRestart: Boolean, callback: AccountCreatorCallback)
}

suspend fun AccountCreator.createAccount(networkId: String, forceRestart: Boolean = false): Account = suspendCoroutine { continuation ->
    this.createAccount(networkId, forceRestart) { err, account ->
        if (err != null) {
            continuation.resumeWithException(err)
        } else {
            continuation.resume(account)
        }
    }
}

suspend fun AccountCreator.importAccount(networkId: String, seedPhrase: String, forceRestart: Boolean = false): Account = suspendCoroutine { continuation ->
    this.importAccount(networkId, seedPhrase, forceRestart) { err, account ->
        if (err != null) {
            continuation.resumeWithException(err)
        } else {
            continuation.resume(account)
        }
    }
}