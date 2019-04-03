@file:Suppress("DEPRECATION")
package me.uport.sdk.identity

import android.content.Context
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.UportHDSigner.Companion.GENERIC_DEVICE_KEY_DERIVATION_PATH
import com.uport.sdk.signer.UportHDSigner.Companion.GENERIC_RECOVERY_DERIVATION_PATH
import com.uport.sdk.signer.computeAddressForPath
import com.uport.sdk.signer.createHDSeed
import com.uport.sdk.signer.encryption.KeyProtection
import com.uport.sdk.signer.importHDSeed
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.uport.sdk.core.IFuelTokenProvider
import me.uport.sdk.core.Networks
import me.uport.sdk.core.onCreateFuelToken
import me.uport.sdk.identity.ProgressPersistence.AccountCreationState
import me.uport.sdk.identity.ProgressPersistence.PersistentBundle
import me.uport.sdk.identity.endpoints.Unnu

/**
 * [Account] manager backed by a [UportHDSigner] that controls a
 * [uPort proxy account](https://github.com/uport-project/uport-identity).
 *
 * This type of account supports meta-transactions but require fuel-tokens
 *
 * **Work on this identity model is on hold and no support is available. Please use [KPAccountCreator]**
 */
@Deprecated("Work on this identity model is on hold and no support is available. Please use [KPAccountCreator]")
class MetaIdentityAccountCreator(
        private val context: Context,
        private val fuelTokenProvider: IFuelTokenProvider) : AccountCreator {

    private val progress: ProgressPersistence = ProgressPersistence(context)

    private val signer = UportHDSigner()

    /**
     * Creates a new identity on the uPort platform.
     *
     * If the identity creation process is interrupted by process death,
     * it will try to restart from the state it left off
     *
     * FIXME: prevent multiple calls to this method during the same lifecycle
     *
     * To force the creation of a new identity, use [forceRestart]
     */
    @Suppress("LabeledExpression", "ComplexMethod")
    private fun createOrImportAccount(networkId: String, phrase: String?, forceRestart: Boolean): Account = runBlocking {

        var (state, oldBundle) = if (forceRestart) {
            (AccountCreationState.NONE to PersistentBundle())
        } else {
            progress.restore()
        }

        advanceCreationState@ while (state != AccountCreationState.COMPLETE) {
            when (state) {

                AccountCreationState.NONE -> {
                    val (rootAddress, _) = if (phrase.isNullOrEmpty()) {
                        signer.createHDSeed(context, KeyProtection.Level.SIMPLE)
                    } else {
                        signer.importHDSeed(context, KeyProtection.Level.SIMPLE, phrase)
                    }
                    val bundle = oldBundle.copy(rootAddress = rootAddress)
                    progress.save(AccountCreationState.ROOT_KEY_CREATED, bundle)
                    continue@advanceCreationState
                }

                AccountCreationState.ROOT_KEY_CREATED -> {
                    val (deviceAddress, _) = signer.computeAddressForPath(context, oldBundle.rootAddress, GENERIC_DEVICE_KEY_DERIVATION_PATH, "")
                    val bundle = oldBundle.copy(deviceAddress = deviceAddress)
                    progress.save(AccountCreationState.DEVICE_KEY_CREATED, bundle)
                    continue@advanceCreationState
                }

                AccountCreationState.DEVICE_KEY_CREATED -> {
                    val (recoveryAddress, _) = signer.computeAddressForPath(context, oldBundle.rootAddress, GENERIC_RECOVERY_DERIVATION_PATH, "")
                    val detail = oldBundle.copy(recoveryAddress = recoveryAddress)
                    progress.save(AccountCreationState.RECOVERY_KEY_CREATED, detail)
                    continue@advanceCreationState
                }

                AccountCreationState.RECOVERY_KEY_CREATED -> {
                    val fuelToken = fuelTokenProvider.onCreateFuelToken(oldBundle.deviceAddress)
                    val bundle = oldBundle.copy(fuelToken = fuelToken)
                    progress.save(AccountCreationState.FUEL_TOKEN_OBTAINED, bundle)
                    continue@advanceCreationState
                }

                AccountCreationState.FUEL_TOKEN_OBTAINED -> {

                    val identityInfo = Unnu().requestIdentityCreation(
                            oldBundle.deviceAddress,
                            oldBundle.recoveryAddress,
                            networkId,
                            oldBundle.fuelToken
                    )
                    val bundle = oldBundle.copy(txHash = identityInfo.txHash ?: "")
                    progress.save(AccountCreationState.PROXY_CREATION_SENT, bundle)
                    continue@advanceCreationState
                }

                AccountCreationState.PROXY_CREATION_SENT -> {
                    var pollingDelay = POLLING_INTERVAL
                    while (state != AccountCreationState.COMPLETE) {

                        val identityInfo = Unnu().lookupIdentityInfo(oldBundle.deviceAddress)

                        //if (err != null) {
                        //    //FIXME: an error here does not necessarily mean a failure; the flow splits here based on type of failure, for example Unnu returns 404 if the proxy hasn't been mined yet
                        //}

                        if (identityInfo != Unnu.IdentityInfo.blank) {
                            val proxyAddress = identityInfo.proxyAddress ?: ""
                            val acc = Account(
                                    oldBundle.rootAddress,
                                    oldBundle.deviceAddress,
                                    networkId,
                                    proxyAddress,
                                    identityInfo.managerAddress,
                                    Networks.get(networkId).txRelayAddress,
                                    oldBundle.fuelToken,
                                    AccountType.MetaIdentityManager
                            )
                            state = AccountCreationState.COMPLETE
                            progress.save(state, oldBundle.copy(partialAccount = acc))

                            return@runBlocking acc
                        }

                        pollingDelay = Math.round(pollingDelay * BACKOFF_FACTOR).toLong()
                        delay(pollingDelay)
                    }
                }
                AccountCreationState.COMPLETE -> {
                    return@runBlocking oldBundle.partialAccount
                }
                else ->
                    throw AccountCreationError(state)
            }
        }
        throw AccountCreationError(state)
    }

    /**
     * Signal a known error encountered during account creation
     */
    class AccountCreationError(state: AccountCreationState) : RuntimeException("Exhausted account creation options, ${state.name}")

    override suspend fun createAccount(networkId: String, forceRecreate: Boolean): Account {
        return createOrImportAccount(networkId, null, forceRecreate)
    }

    override suspend fun importAccount(networkId: String, seedPhrase: String, forceRecreate: Boolean): Account {
        return createOrImportAccount(networkId, seedPhrase, forceRecreate)
    }

    override suspend fun deleteAccount(handle: String) {
        signer.deleteSeed(context, handle)
    }

    companion object {
        private const val BACKOFF_FACTOR = 1.1f
        private const val POLLING_INTERVAL = 5000L
    }

}