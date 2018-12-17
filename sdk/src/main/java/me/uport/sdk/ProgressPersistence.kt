package me.uport.sdk

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Json
import org.kethereum.functions.encodeRLP
import org.kethereum.model.Transaction

class ProgressPersistence(context: Context) {

    private val prefs: SharedPreferences

    init {
        this.prefs = context.getSharedPreferences(TRANSACTIONS_PREFS, Context.MODE_PRIVATE)
    }

    /**
     * XXX: This should hold the state of ethereum transactions
     */
    enum class PendingTransactionState {
        NONE,
        TRANSACTION_BUILT,
        TRANSACTION_SIGNED,
        TRANSACTION_SENT,
        TRANSACTION_CONFIRMED,
    }

    /**
     * Wrapper for intermediate states of transaction lifecycle
     */
    internal data class PersistentBundle(
            @Json(name = "unsigned")
            val unsigned: Transaction = Transaction(),

            @Json(name = "signed")
            val signed: ByteArray = byteArrayOf(),

            @Json(name = "txHash")
            val txHash: String = "",

            @Json(name = "blockHash")
            val blockHash: String = "",

            @Json(name = "ordinal")
            val ordinal: Int = 0

    ) {
        fun toJson() = jsonAdapter.toJson(this) ?: ""

        companion object {
            fun fromJson(json: String): PersistentBundle = try {
                val something = Transaction()
                something.encodeRLP()
                jsonAdapter.fromJson(json)
                        ?: PersistentBundle()
            } catch (err: Exception) {
                PersistentBundle()
            }

            private val jsonAdapter = moshi.adapter<PersistentBundle>(PersistentBundle::class.java)
        }
    }

    internal fun save(temp: PersistentBundle = PersistentBundle(),
                      label: String) {

        prefs.edit()
                .putString(label, temp.toJson())
                .apply()
    }

    internal fun restore(label: String): Pair<PendingTransactionState, PersistentBundle> {
        val serialized = prefs.getString(label, "").orEmpty()
        val ordinal = PersistentBundle.fromJson(serialized).ordinal
        val state = PendingTransactionState.values()[ordinal]
        return (state to PersistentBundle.fromJson(serialized))
    }

    internal fun contains(label: String): Boolean {
        return prefs.contains(label)
    }

    companion object {
        private const val TRANSACTIONS_PREFS = "transactions_pref"
    }

}