package me.uport.sdk

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.uport.sdk.core.EthNetwork
import me.uport.sdk.core.IFuelTokenProvider
import me.uport.sdk.core.Networks
import me.uport.sdk.core.UI
import me.uport.sdk.ethrdid.EthrDIDResolver
import me.uport.sdk.identity.*
import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.universaldid.UniversalDID
import me.uport.sdk.uportdid.UportDIDResolver
import me.uport.sdk.httpsdid.HttpsDIDResolver
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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

        UniversalDID.registerResolver(UportDIDResolver())
        val defaultRPC = JsonRPC(Networks.mainnet.rpcUrl)
        UniversalDID.registerResolver(EthrDIDResolver(defaultRPC))
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
    fun createAccount(network: EthNetwork, seedPhrase: String? = null, completion: AccountCreatorCallback) {
        return createAccount(network.network_id, seedPhrase, completion)
    }

    /**
     * Creates an account (the [defaultAccount]) in a coroutine context
     * For v1 of this SDK, there's only one account supported.
     * If an account has already been created, that one will be returned.
     * If the process has already been started before, it will continue where it left off.
     * The created account is saved as [defaultAccount] before returning with a result
     *
     */
    suspend fun createAccount(network: EthNetwork, seedPhrase: String? = null): Account = suspendCoroutine { cont ->
        this.createAccount(network, seedPhrase) { err, acc ->
            if (err != null) {
                cont.resumeWithException(err)
            } else {
                cont.resume(acc)
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
    private fun createAccount(networkId: String, seedPhrase: String?, completion: AccountCreatorCallback) {
        if (!initialized) {
            throw UportNotInitializedException()
        }

        GlobalScope.launch {
            try {
                val acc = if (seedPhrase.isNullOrBlank()) {
                    accountCreator.createAccount(networkId)
                } else {
                    accountCreator.importAccount(networkId, seedPhrase)
                }
                accountStorage?.upsert(acc)
                defaultAccount = defaultAccount ?: acc

                withContext(UI) { completion(null, if (acc.handle == defaultAccount?.handle) defaultAccount!! else acc) }
            } catch (err: Exception) {
                withContext(UI) { completion(err, Account.blank) }
            }
        }
    }

    fun getAccount(handle: String) = accountStorage?.get(handle)

    fun allAccounts() = accountStorage?.all() ?: emptyList()

    fun deleteAccount(rootHandle: String) {
        if (!initialized) {
            throw UportNotInitializedException()
        }

        accountCreator.deleteAccount(rootHandle)
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
