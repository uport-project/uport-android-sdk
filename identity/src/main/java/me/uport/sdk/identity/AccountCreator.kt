package me.uport.sdk.identity

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

typealias AccountCreatorCallback = (err: Exception?, acc: Account) -> Unit

interface AccountCreator {
    fun createAccount(networkId: String, forceRestart: Boolean = false, callback: AccountCreatorCallback)

    fun importAccount(networkId: String, seedPhrase: String, forceRestart: Boolean, callback: AccountCreatorCallback)

    fun deleteAccount(handle: String)
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

fun AccountCreator.deleteAccount(acc: Account) = this.deleteAccount(acc.handle)