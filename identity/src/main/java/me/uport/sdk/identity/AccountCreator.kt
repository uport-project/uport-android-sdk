package me.uport.sdk.identity

typealias AccountCreatorCallback = (err: Exception?, acc: Account) -> Unit

interface AccountCreator {
    suspend fun createAccount(networkId: String, forceRestart: Boolean = false): Account
    suspend fun importAccount(networkId: String, seedPhrase: String, forceRestart: Boolean = false): Account
    suspend fun deleteAccount(handle: String)
}