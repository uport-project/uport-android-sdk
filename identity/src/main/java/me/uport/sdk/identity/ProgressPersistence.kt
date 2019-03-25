package me.uport.sdk.identity

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ProgressPersistence(context: Context) {

    private val prefs: SharedPreferences

    init {
        this.prefs = context.getSharedPreferences(ACCOUNT_CREATOR_PREFS, Context.MODE_PRIVATE)
    }

    /**
     * XXX: This should hold the state of the uport-account creation process.
     */
    enum class AccountCreationState {
        NONE,
        ROOT_KEY_CREATED,
        DEVICE_KEY_CREATED,
        RECOVERY_KEY_CREATED,
        FUEL_TOKEN_OBTAINED,
        PROXY_CREATION_SENT,
        PROXY_CREATION_MINED,
        COMPLETE
    }

    /**
     * Wrapper for intermediate states of account creation
     */
    @Serializable
    internal data class PersistentBundle(
            @SerialName("rootAddress")
            val rootAddress: String = "",

            @SerialName("devKey")
            val deviceAddress: String = "",

            @SerialName("recoveryKey")
            val recoveryAddress: String = "",

            @SerialName("fuelToken")
            val fuelToken: String = "",

            @SerialName("txHash")
            val txHash: String = "",

            @SerialName("partialAccount")
            val partialAccount: MetaIdentityAccount = MetaIdentityAccount.blank
    ) {
        fun toJson() = Json.stringify(PersistentBundle.serializer(), this)

        companion object {
            fun fromJson(json: String): PersistentBundle = try {
                Json.nonstrict.parse(PersistentBundle.serializer(), json)
            } catch (err: Exception) {
                PersistentBundle()
            }
        }
    }

    internal fun save(
            state: AccountCreationState,
            temp: PersistentBundle = PersistentBundle()) {

        prefs.edit()
                .putInt(ACCOUNT_CREATION_PROGRESS, state.ordinal)
                .putString(ACCOUNT_CREATION_DETAIL, temp.toJson())
                .apply()
    }

    internal fun restore(): Pair<AccountCreationState, PersistentBundle> {
        val persistedOrdinal = prefs.getInt(ACCOUNT_CREATION_PROGRESS, AccountCreationState.NONE.ordinal)
        val state = AccountCreationState.values()[persistedOrdinal]
        val serialized = prefs.getString(ACCOUNT_CREATION_DETAIL, "") ?: ""

        return (state to PersistentBundle.fromJson(serialized))
    }

    companion object {
        private const val ACCOUNT_CREATOR_PREFS = "resurrect_promise"
        private const val ACCOUNT_CREATION_PROGRESS = "account_creation_progress"
        private const val ACCOUNT_CREATION_DETAIL = "account_creation_detail"
    }

}