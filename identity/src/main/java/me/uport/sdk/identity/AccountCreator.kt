package me.uport.sdk.identity

typealias AccountCreatorCallback = (err: Exception?, acc: Account) -> Unit

interface AccountCreator {
    suspend fun createAccount(networkId: String, forceRecreate: Boolean = false): Account

    suspend fun importAccount(networkId: String, seedPhrase: String, forceRecreate: Boolean = false): Account

    suspend fun deleteAccount(handle: String)
}