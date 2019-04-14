package me.uport.sdk

import android.content.SharedPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.uport.sdk.identity.Account
import me.uport.sdk.identity.AccountType
import me.uport.sdk.identity.HDAccount
import me.uport.sdk.identity.MetaIdentityAccount

interface AccountStorage {
    fun upsert(newAcc: Account)

    fun get(handle: String): Account?

    fun delete(handle: String)

    fun all(): List<Account>

    fun upsertAll(list: Collection<Account>)
}


/**
 * An account storage mechanism that relies on [SharedPreferences] for persistence to disk
 *
 * Accounts are serialized then wrapped in an AccountHolder then along with the AccountType and isDefault
 *
 * Accounts are loaded during construction and then relayed from memory
 */
class SharedPrefsAccountStorage(
        private val prefs: SharedPreferences
) : AccountStorage {

    private val accounts = mapOf<String, AccountHolder>().toMutableMap()

    init {
        prefs.getStringSet(KEY_ACCOUNTS, emptySet())
                .orEmpty()
                .forEach { serialized ->
                    val accountHolder = try {
                        AccountHolder.fromJson(serialized)
                    } catch (ex: Exception) {
                        null
                    }

                    accountHolder.let {
                        val account = fetchAccountFromHolder(accountHolder)
                        if (account != null) {
                            upsert(account)
                        }
                    }
                }
    }

    override fun upsert(newAcc: Account) {
        accounts[newAcc.handle] = buildAccountHolder(newAcc)
        persist()
    }

    override fun upsertAll(list: Collection<Account>) {
        list.forEach {
            accounts[it.handle] = buildAccountHolder(it)
        }

        persist()
    }

    override fun get(handle: String): Account? {

        val holder: AccountHolder? = accounts[handle]

        return fetchAccountFromHolder(holder)
    }

    override fun delete(handle: String) {
        accounts.remove(handle)

        if (getDefaultAccount()?.handle.equals(handle)) {
            persistDefault("")
        }

        persist()
    }

    override fun all(): List<Account> = fetchAllAccounts()

    private fun persist() {
        prefs.edit()
                .putStringSet(KEY_ACCOUNTS, accounts.values.map { it.toJson() }.toSet())
                .apply()
    }

    /**
     *  Saves the account handle of the default account
     */
    fun setAsDefault(accountHandle: String) {
        persistDefault(accountHandle)
    }

    /**
     *  Deserializes default account from the saved account Holder which
     */
    fun getDefaultAccount(): Account? {
        val accountHandle = prefs.getString(KEY_DEFAULT_ACCOUNT, "") ?: ""
        val defaultAccountHolder = accounts[accountHandle]
        if (defaultAccountHolder != null && defaultAccountHolder != AccountHolder.blank) {
            return fetchAccountFromHolder(defaultAccountHolder)
        } else {
            return null
        }
    }

    private fun persistDefault(serializedAccountHolder: String) {
        prefs.edit()
                .putString(KEY_DEFAULT_ACCOUNT, serializedAccountHolder)
                .apply()
    }

    companion object {
        private const val KEY_ACCOUNTS = "accounts"
        private const val KEY_DEFAULT_ACCOUNT = "default_account"
    }

    @Suppress("UnsafeCast")
    private fun buildAccountHolder(account: Account): AccountHolder {

        val acc = when (account.type) {
            AccountType.HDKeyPair -> (account as HDAccount).toJson()
            AccountType.MetaIdentityManager -> (account as MetaIdentityAccount).toJson()
            else -> throw IllegalArgumentException("Storage not supported AccountType ${account.type}")
        }

        return AccountHolder(acc, account.type.toString())
    }

    private fun fetchAccountFromHolder(holder: AccountHolder?): Account? {

        return when (holder?.type) {
            AccountType.HDKeyPair.toString() -> HDAccount.fromJson(holder.account)
            AccountType.MetaIdentityManager.toString() -> MetaIdentityAccount.fromJson(holder.account)
            else -> null
        }
    }

    private fun fetchAllAccounts(): List<Account> {
        val listOfAccounts = mutableListOf<Account>()

        accounts.forEach {
            val account = fetchAccountFromHolder(it.value)
            if (account != null) {
                listOfAccounts.add(account)
            }
        }

        return listOfAccounts.toList()
    }
}


/**
 * Used to wrap any type of account before it is stored
 */
@Serializable
data class AccountHolder(
        val account: String,
        val type: String
) {

    /**
     * serializes accountHolder
     */
    fun toJson(pretty: Boolean = false): String = if (pretty) Json.indented.stringify(AccountHolder.serializer(), this) else Json.stringify(AccountHolder.serializer(), this)

    companion object {

        val blank = AccountHolder("", "")

        /**
         * de-serializes accountHolder
         */
        fun fromJson(serializedAccountHolder: String): AccountHolder? {
            if (serializedAccountHolder.isEmpty()) {
                return null
            }

            return Json.parse(AccountHolder.serializer(), serializedAccountHolder)
        }
    }
}