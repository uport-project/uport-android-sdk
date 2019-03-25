package me.uport.sdk.identity

typealias AccountCreatorCallback = (err: Exception?, acc: HDAccount) -> Unit

/**
 * Interface describing an [HDAccount] manager that can create, import and delete accounts
 */
interface AccountCreator {

    /**
     * Create an [HDAccount] rooted in a given [networkId].
     * The process can be restarted if [forceRecreate] is set to true
     */
    suspend fun createAccount(networkId: String, forceRecreate: Boolean = false): Account

    /**
     * Create an [HDAccount] rooted in a given [networkId] and based on a seed phrase
     * The process can be restarted if [forceRecreate] is set to true
     */
    suspend fun importAccount(networkId: String, seedPhrase: String, forceRecreate: Boolean = false): Account

    /**
     * Deletes a previously created account from the underlying storage
     */
    suspend fun deleteAccount(handle: String)
}