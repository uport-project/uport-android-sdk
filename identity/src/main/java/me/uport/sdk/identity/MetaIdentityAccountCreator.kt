package me.uport.sdk.identity

import android.content.Context
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.encryption.KeyProtection
import kotlinx.coroutines.experimental.delay
import me.uport.sdk.core.IFuelTokenProvider
import me.uport.sdk.core.Networks
import me.uport.sdk.identity.ProgressPersistence.AccountCreationState
import me.uport.sdk.identity.ProgressPersistence.PersistentBundle
import me.uport.sdk.identity.endpoints.lookupIdentityInfo
import me.uport.sdk.identity.endpoints.requestIdentityCreation


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
    private suspend fun createOrImportAccount(networkId: String, phrase: String?, forceRestart: Boolean): Account {
        var (state, oldBundle) = if (forceRestart) {
            (AccountCreationState.NONE to PersistentBundle())
        } else {
            progress.restore()
        }

        when (state) {
            AccountCreationState.NONE -> {
                if (phrase.isNullOrEmpty()) {
                    val (rootAddress, _) = failSave {
                        signer.createHDSeed(context, KeyProtection.Level.SIMPLE)
                    }
                    val bundle = oldBundle.copy(rootAddress = rootAddress)
                    progress.save(AccountCreationState.ROOT_KEY_CREATED, bundle)
                    return createAccount(networkId, false)

                } else {
                    val (rootAddress, _) = failSave {
                        signer.importHDSeed(context, KeyProtection.Level.SIMPLE, phrase!!)
                    }
                    val bundle = oldBundle.copy(rootAddress = rootAddress)
                    progress.save(AccountCreationState.ROOT_KEY_CREATED, bundle)
                    return createAccount(networkId, false)
                }
            }

            AccountCreationState.ROOT_KEY_CREATED -> {
                val (deviceAddress, _) = signer.computeAddressForPath(context, oldBundle.rootAddress, Account.GENERIC_DEVICE_KEY_DERIVATION_PATH, "")
                val bundle = oldBundle.copy(deviceAddress = deviceAddress)
                progress.save(AccountCreationState.DEVICE_KEY_CREATED, bundle)
                return createAccount(networkId, false)
            }

            AccountCreationState.DEVICE_KEY_CREATED -> {
                val (recoveryAddress, _) = signer.computeAddressForPath(context, oldBundle.rootAddress, Account.GENERIC_RECOVERY_DERIVATION_PATH, "")
                val detail = oldBundle.copy(recoveryAddress = recoveryAddress)
                progress.save(AccountCreationState.RECOVERY_KEY_CREATED, detail)
                return createAccount(networkId, false)
            }

            AccountCreationState.RECOVERY_KEY_CREATED -> {
                val fuelToken = fuelTokenProvider.onCreateFuelToken(oldBundle.deviceAddress)
                val bundle = oldBundle.copy(fuelToken = fuelToken)
                progress.save(AccountCreationState.FUEL_TOKEN_OBTAINED, bundle)
                return createAccount(networkId, false)
            }

            AccountCreationState.FUEL_TOKEN_OBTAINED -> {
                val identityInfo = requestIdentityCreation(oldBundle.deviceAddress, oldBundle.recoveryAddress, networkId, oldBundle.fuelToken)
                val bundle = oldBundle.copy(txHash = identityInfo.txHash ?: "")
                progress.save(AccountCreationState.PROXY_CREATION_SENT, bundle)
                return createAccount(networkId, false)
            }

            AccountCreationState.PROXY_CREATION_SENT -> {
                val identityInfo = retry({ state != AccountCreationState.COMPLETE }) {
                    lookupIdentityInfo(oldBundle.deviceAddress)
                }

                //FIXME: an error here does not necessarily mean a failure; the flow splits here based on type of failure, for example Unnu returns 404 if the proxy hasn't been mined yet
                val proxyAddress = identityInfo.proxyAddress ?: ""
                val account = Account(
                        oldBundle.rootAddress,
                        oldBundle.deviceAddress,
                        networkId,
                        proxyAddress,
                        identityInfo.managerAddress,
                        Networks.get(networkId).txRelayAddress,
                        oldBundle.fuelToken,
                        SignerType.MetaIdentityManager
                )
                state = AccountCreationState.COMPLETE
                progress.save(state, oldBundle.copy(partialAccount = account))
                return account
            }

            AccountCreationState.COMPLETE -> {
                return oldBundle.partialAccount
            }
            else -> throw RuntimeException("Exhausted account creation options, ${state.name}")
        }

    }

    private suspend fun <T> retry(condition: () -> Boolean, block: suspend () -> T): T {
        var pollingDelay = POLLING_INTERVAL
        while (condition()) {
            try {
                return block()
            } catch (exception: Exception) {
                // TODO: Handle exception
            }
            delay(pollingDelay)
            pollingDelay = Math.round(pollingDelay * BACKOFF_FACTOR).toLong()
        }
        return block()
    }

    override suspend fun createAccount(networkId: String, forceRestart: Boolean): Account {
        return createOrImportAccount(networkId, null, forceRestart)
    }

    override suspend fun importAccount(networkId: String, seedPhrase: String, forceRestart: Boolean): Account {
        return createOrImportAccount(networkId, seedPhrase, forceRestart)
    }

    override suspend fun deleteAccount(handle: String) {
        signer.deleteSeed(context, handle)
    }

    private suspend fun <T> failSave(block: suspend () -> T): T {
        return try {
            block()
        } catch (exception: Exception) {
            progress.save(AccountCreationState.NONE)
            throw exception
        }
    }

    companion object {
        private const val BACKOFF_FACTOR = 1.1f
        private const val POLLING_INTERVAL = 5000L
    }

}