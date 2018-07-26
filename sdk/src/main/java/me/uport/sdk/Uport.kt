package me.uport.sdk

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import me.uport.sdk.core.EthNetwork
import me.uport.sdk.identity.*
import kotlin.coroutines.experimental.suspendCoroutine

object Uport {

    private var initialized = false

    @SuppressLint("StaticFieldLeak")
    private lateinit var config: Configuration
    private lateinit var prefs: SharedPreferences

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
    fun createAccount(network: EthNetwork, completion: AccountCreatorCallback) {
        return createAccount(network.network_id, completion)
    }

    /**
     * Creates an account (the [defaultAccount]) in a coroutine context
     * For v1 of this SDK, there's only one account supported.
     * If an account has already been created, that one will be returned.
     * If the process has already been started before, it will continue where it left off.
     * The created account is saved as [defaultAccount] before returning with a result
     *
     */
    suspend fun createAccount(network: EthNetwork): Account = suspendCoroutine { cont ->
        this.createAccount(network) { err, acc ->
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
    private fun createAccount(networkId: String, completion: AccountCreatorCallback) {
        if (!initialized) {
            throw UportNotInitializedException()
        }

        //single account limitation should disappear in future versions
        if (defaultAccount != null) {
            launch(UI) { completion(null, defaultAccount!!) }
            return
        }

        launch {
            try {
                val creator = KPAccountCreator(config.applicationContext)
                val acc = creator.createAccount(networkId)
                prefs.edit().putString(DEFAULT_ACCOUNT, acc.toJson()).apply()
                defaultAccount = defaultAccount ?: acc

                launch(UI) { completion(null, acc) }
            } catch (err: Exception) {
                launch(UI) { completion(err, Account.blank) }
            }
        }
    }

    fun deleteAccount() {
        TODO("not implemented")
    }

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
