package me.uport.sdk

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import kotlinx.coroutines.experimental.launch
import me.uport.sdk.core.EthNetwork
import me.uport.sdk.core.UI
import me.uport.sdk.identity.*
import kotlin.coroutines.experimental.suspendCoroutine

@SuppressLint("StaticFieldLeak")
object Uport {

    private var initialized = false

    private lateinit var config: Configuration

    private lateinit var prefs: SharedPreferences

    private lateinit var accountCreator: AccountCreator

    var defaultAccount: Account? = null

    private const val UPORT_CONFIG: String = "uport_sdk_prefs"

    private const val DEFAULT_ACCOUNT: String = "default_account"

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
        prefs = context.getSharedPreferences(UPORT_CONFIG, MODE_PRIVATE)

        accountCreator = KPAccountCreator(context)

        val serializedAccount = prefs.getString(DEFAULT_ACCOUNT, "")
        defaultAccount = Account.fromJson(serializedAccount)

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

        //FIXME: single account limitation should disappear in future versions
        if (defaultAccount != null) {
            launch(UI) { completion(null, defaultAccount!!) }
            return
        }

        launch {
            try {
                val acc = if (seedPhrase.isNullOrBlank()) {
                    accountCreator.createAccount(networkId)
                } else {
                    accountCreator.importAccount(networkId, seedPhrase!!)
                }
                prefs.edit().putString(DEFAULT_ACCOUNT, acc.toJson()).apply()
                defaultAccount = defaultAccount ?: acc

                launch(UI) { completion(null, acc) }
            } catch (err: Exception) {
                launch(UI) { completion(err, Account.blank) }
            }
        }
    }

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
