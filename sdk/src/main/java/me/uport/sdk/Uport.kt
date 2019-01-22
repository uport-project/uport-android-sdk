package me.uport.sdk

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.uport.sdk.core.EthNetwork
import me.uport.sdk.core.IFuelTokenProvider
import me.uport.sdk.core.Networks
import me.uport.sdk.ethrdid.EthrDIDResolver
import me.uport.sdk.httpsdid.HttpsDIDResolver
import me.uport.sdk.identity.Account
import me.uport.sdk.identity.AccountCreator
import me.uport.sdk.identity.AccountCreatorCallback
import me.uport.sdk.identity.KPAccountCreator
import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.universaldid.UniversalDID
import me.uport.sdk.uportdid.UportDIDResolver

@SuppressLint("StaticFieldLeak")
object Uport {

    private var initialized = false

    private lateinit var config: Configuration

    private lateinit var prefs: SharedPreferences

    private lateinit var accountCreator: AccountCreator

    private var defaultAccountHandle = ""

    var defaultAccount: Account?
        get() = accountStorage?.get(defaultAccountHandle)
        set(value) {
            val newDefault = value?.copy(isDefault = true)
            @Suppress("LiftReturnOrAssignment")
            if (newDefault == null) {
                accountStorage?.delete(defaultAccountHandle)
                defaultAccountHandle = ""
            } else {
                val oldAccounts = accountStorage
                        ?.all()
                        ?.map { it.copy(isDefault = false) }
                        ?: emptyList()
                accountStorage?.upsertAll(oldAccounts + newDefault)
                defaultAccountHandle = newDefault.handle
            }
        }

    private var accountStorage: AccountStorage? = null

    private const val UPORT_CONFIG: String = "uport_sdk_prefs"

    private const val OLD_DEFAULT_ACCOUNT: String = "default_account"

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

        accountCreator = KPAccountCreator(context)

        prefs = context.getSharedPreferences(UPORT_CONFIG, MODE_PRIVATE)

        accountStorage = SharedPrefsAccountStorage(prefs).apply {
            this.all().forEach {
                if (it.isDefault == true) {
                    defaultAccountHandle = it.handle
                }
            }
        }

        prefs.getString(OLD_DEFAULT_ACCOUNT, "")
                ?.let { Account.fromJson(it) }
                ?.let {
                    accountStorage?.upsert(it.copy(isDefault = true))
                    prefs.edit().remove(OLD_DEFAULT_ACCOUNT).apply()
                }

        UniversalDID.registerResolver(UportDIDResolver(JsonRPC(Networks.rinkeby.rpcUrl)))
        UniversalDID.registerResolver(EthrDIDResolver(JsonRPC(Networks.mainnet.rpcUrl)))
        UniversalDID.registerResolver(HttpsDIDResolver())

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
    @Deprecated("use the suspend variant of this method")
    fun createAccount(network: EthNetwork, seedPhrase: String? = null, completion: AccountCreatorCallback) {
        GlobalScope.launch {
            try {
                val account = createAccount(network.networkId, seedPhrase)
                completion(null, account)
            } catch (ex: Exception) {
                completion(ex, Account.blank)
            }

        }
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
    suspend fun createAccount(networkId: String, seedPhrase: String? = null): Account {
        if (!initialized) {
            throw UportNotInitializedException()
        }

        val newAccount = if (seedPhrase.isNullOrBlank()) {
            accountCreator.createAccount(networkId)
        } else {
            accountCreator.importAccount(networkId, seedPhrase)
        }
        accountStorage?.upsert(newAccount)
        defaultAccount = defaultAccount ?: newAccount
        val result = if (newAccount.handle == defaultAccount?.handle) {
            defaultAccount ?: newAccount
        } else {
            newAccount
        }
        return result
    }

    fun getAccount(handle: String) = accountStorage?.get(handle)

    fun allAccounts() = accountStorage?.all() ?: emptyList()

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

    class Configuration {

        lateinit var fuelTokenProvider: IFuelTokenProvider
        lateinit var applicationContext: Context

        @Suppress("unused")
        fun setFuelTokenProvider(provider: IFuelTokenProvider): Configuration {
            this.fuelTokenProvider = provider
            return this
        }

        fun setApplicationContext(context: Context): Configuration {
            this.applicationContext = context.applicationContext
            return this
        }

    }
}
