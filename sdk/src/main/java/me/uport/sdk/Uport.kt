package me.uport.sdk

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.uport.sdk.core.EthNetwork
import me.uport.sdk.core.Networks
import me.uport.sdk.ethrdid.EthrDIDResolver
import me.uport.sdk.httpsdid.HttpsDIDResolver
import me.uport.sdk.identity.*
import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.universaldid.UniversalDID
import me.uport.sdk.uportdid.UportDIDResolver

@SuppressLint("StaticFieldLeak")
object Uport {

    private var initialized = false

    private lateinit var config: Configuration

    private lateinit var oldPrefs: SharedPreferences

    private lateinit var prefs: SharedPreferences

    private lateinit var accountCreator: HDAccountCreator

    private lateinit var accountStorage: SharedPrefsAccountStorage

    @Suppress("UnsafeCast")
    var defaultAccount: HDAccount?
        get() = accountStorage.getDefaultAccount() as HDAccount?
        set(value) {
            accountStorage.setAsDefault(value?.handle ?: "")
        }

    private const val OLD_UPORT_CONFIG: String = "uport_sdk_prefs"

    private const val UPORT_CONFIG: String = "uport_sdk_prefs_new"

    /**
     * Initialize the Uport SDK.
     *
     * This method needs to be called as early as possible in the app lifecycle, ideally in the
     * `onCreate()` method of your [android.app.Application] instance
     *
     * Other methods will throw [UportNotInitializedException] if this hasn't been previously called
     */
    fun initialize(configuration: Configuration) {
        this.config = configuration

        val context = config.applicationContext

        accountCreator = HDAccountCreator(context)

        oldPrefs = context.getSharedPreferences(OLD_UPORT_CONFIG, MODE_PRIVATE)

        prefs = context.getSharedPreferences(UPORT_CONFIG, MODE_PRIVATE)

        accountStorage = SharedPrefsAccountStorage(prefs)

        UniversalDID.registerResolver(UportDIDResolver(JsonRPC(configuration.network?.rpcUrl
                ?: Networks.rinkeby.rpcUrl)))

        val ethrDidRpcUrl = configuration.network?.rpcUrl ?: Networks.mainnet.rpcUrl
        val ethrDidRegistry = configuration.network?.ethrDidRegistry
                ?: Networks.mainnet.ethrDidRegistry
        UniversalDID.registerResolver(EthrDIDResolver(JsonRPC(ethrDidRpcUrl), ethrDidRegistry))

        UniversalDID.registerResolver(HttpsDIDResolver())

        migrateAccounts(oldPrefs, accountStorage)

        //TODO: weak, make Configuration into a builder and actually make methods fail when not configured
        initialized = true
    }

    /**
     * Creates an account (the [defaultAccount])..
     * For v1 of this SDK, there's only one account supported.
     * If an account has already been created, that one will be returned.
     * If the process has already been started before, it will continue where it left off.
     * The created account is saved as [defaultAccount] before calling back with the result
     *
     * To really create a new account, call [deleteAccount] first.
     */
    suspend fun createAccount(networkId: String, seedPhrase: String? = null): HDAccount {
        if (!initialized) {
            throw UportNotInitializedException()
        }

        val newAccount = if (seedPhrase.isNullOrBlank()) {
            accountCreator.createAccount(networkId)
        }
        else {
            accountCreator.importAccount(networkId, seedPhrase)
        }
        accountStorage.upsert(newAccount)

        defaultAccount = defaultAccount ?: newAccount

        return newAccount
    }

    /**
     * Fetches the account based on the provided handle
     */
    fun getAccount(handle: String) = accountStorage.get(handle)

    /**
     * Fetches all saved accounts
     */
    fun allAccounts() = accountStorage.all()

    fun deleteAccount(rootHandle: String) {
        if (!initialized) {
            throw UportNotInitializedException()
        }

        runBlocking { accountCreator.deleteAccount(rootHandle) }
        if (rootHandle == defaultAccount?.handle) {
            defaultAccount = null
        }
    }

    fun deleteAccount(acc: Account) = deleteAccount(acc.handle)

    internal fun migrateAccounts(oldPrefs: SharedPreferences, accountStorage: AccountStorage) {

        //declare keys for the old account storage
        val KEY_ACCOUNTS = "accounts"
        val KEY_DEFAULT_ACCOUNT = "default_account"

        // only run when associated keys are available
        if (!oldPrefs.contains(KEY_ACCOUNTS) || !oldPrefs.contains(KEY_DEFAULT_ACCOUNT)) {
            return
        }

        // convert all accounts to HDAccount and save in new account manager
        oldPrefs.getStringSet(KEY_ACCOUNTS, emptySet())
                .orEmpty()
                .forEach { serialized ->
                    val account = try {
                        HDAccount.fromJson(serialized)
                    } catch (ex: Exception) {
                        null
                    }

                    account?.let {
                        val accountCopy = it.copy(type = AccountType.HDKeyPair)
                        accountStorage.upsert(accountCopy)
                    }
                }

        // save old default account handle to new storage
        accountStorage.setAsDefault(oldPrefs.getString(KEY_DEFAULT_ACCOUNT, "") ?: "")

        // remove keys from the old prefs
        oldPrefs.edit()
                .remove(KEY_ACCOUNTS)
                .remove(KEY_DEFAULT_ACCOUNT)
                .apply()
    }
}
