package me.uport.sdk.identity

import android.content.Context
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.encryption.KeyProtection
import me.uport.sdk.core.Networks
import me.uport.sdk.identity.ProgressPersistence.AccountCreationState
import me.uport.sdk.identity.ProgressPersistence.PersistentBundle
import me.uport.sdk.identity.endpoints.UnnuIdentityInfo
import me.uport.sdk.identity.endpoints.lookupIdentityInfo
import me.uport.sdk.identity.endpoints.requestIdentityCreation

typealias AccountCreatorCallback = (err: Exception?, acc: Account) -> Unit

class MetaIdentityAccountCreator(
        private val context: Context,
        private val fuelTokenProvider: IFuelTokenProvider) {

    private val progress: ProgressPersistence = ProgressPersistence(context)

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
    fun createAccount(networkId: String, forceRestart: Boolean = false, callback: AccountCreatorCallback) {

        var (state, oldBundle) = if (forceRestart) {
            (AccountCreationState.NONE to PersistentBundle())
        } else {
            progress.restore()
        }

        val signer = UportHDSigner()

        when (state) {

            AccountCreationState.NONE -> {
                signer.createHDSeed(context, KeyProtection.Level.SIMPLE) { err, rootAddress, _ ->
                    if (err != null) {
                        return@createHDSeed fail(err, callback)
                    }
                    val bundle = oldBundle.copy(rootAddress = rootAddress)
                    progress.save(AccountCreationState.ROOT_KEY_CREATED, bundle)
                    return@createHDSeed createAccount(networkId, false, callback)
                }
            }

            AccountCreationState.ROOT_KEY_CREATED -> {
                signer.computeAddressForPath(context, oldBundle.rootAddress, Account.GENERIC_DEVICE_KEY_DERIVATION_PATH, "") { err, deviceAddress, _ ->
                    if (err != null) {
                        return@computeAddressForPath fail(err, callback)
                    }
                    val bundle = oldBundle.copy(deviceAddress = deviceAddress)
                    progress.save(AccountCreationState.DEVICE_KEY_CREATED, bundle)
                    return@computeAddressForPath createAccount(networkId, false, callback)
                }
            }

            AccountCreationState.DEVICE_KEY_CREATED -> {
                signer.computeAddressForPath(context, oldBundle.rootAddress, Account.GENERIC_RECOVERY_DERIVATION_PATH, "") { err, recoveryAddress, _ ->
                    if (err != null) {
                        return@computeAddressForPath fail(err, callback)
                    }
                    val detail = oldBundle.copy(recoveryAddress = recoveryAddress)
                    progress.save(AccountCreationState.RECOVERY_KEY_CREATED, detail)
                    return@computeAddressForPath createAccount(networkId, false, callback)
                }
            }

            AccountCreationState.RECOVERY_KEY_CREATED -> {
                fuelTokenProvider.onCreateFuelToken(oldBundle.deviceAddress) { err, fuelToken ->
                    if (err != null) {
                        return@onCreateFuelToken fail(err, callback)
                    }

                    val bundle = oldBundle.copy(fuelToken = fuelToken)
                    progress.save(AccountCreationState.FUEL_TOKEN_OBTAINED, bundle)
                    return@onCreateFuelToken createAccount(networkId, false, callback)
                }
            }

            AccountCreationState.FUEL_TOKEN_OBTAINED -> {

                requestIdentityCreation(
                        oldBundle.deviceAddress,
                        oldBundle.recoveryAddress,
                        networkId,
                        oldBundle.fuelToken
                ) { err, identityInfo ->
                    if (err != null) {
                        return@requestIdentityCreation fail(err, callback)
                    }
                    val bundle = oldBundle.copy(txHash = identityInfo.txHash ?: "")
                    progress.save(AccountCreationState.PROXY_CREATION_SENT, bundle)

                    return@requestIdentityCreation createAccount(networkId, false, callback)
                }

            }

            AccountCreationState.PROXY_CREATION_SENT -> {
                Thread {
                    var pollingDelay = POLLING_INTERVAL
                    while (state != AccountCreationState.COMPLETE) {

                        lookupIdentityInfo(oldBundle.deviceAddress) { _, identityInfo ->

                            //if (err != null) {
                            //    //FIXME: an error here does not necessarily mean a failure; the flow splits here based on type of failure, for example Unnu returns 404 if the proxy hasn't been mined yet
                            //    return@lookupIdentityInfo fail(context, err, callback)
                            //}

                            if (identityInfo != UnnuIdentityInfo.blank) {
                                val proxyAddress = identityInfo.proxyAddress ?: ""
                                val acc = Account(
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
                                progress.save(state, oldBundle.copy(partialAccount = acc))

                                return@lookupIdentityInfo callback(null, acc)
                            }

                        }

                        pollingDelay = Math.round(pollingDelay * BACKOFF_FACTOR).toLong()
                        //FIXME: use saner polling model.. coroutines maybe?
                        Thread.sleep(pollingDelay)
                    }
                }.start()
            }
            AccountCreationState.COMPLETE -> {
                return callback(null, oldBundle.partialAccount)
            }
            else ->
                return callback(RuntimeException("Exhausted account creation options, ${state.name}"), Account.blank)
        }
    }

    private fun fail(err: Exception, callback: AccountCreatorCallback) {
        progress.save(AccountCreationState.NONE)
        return callback(err, Account.blank)
    }

    companion object {
        private const val BACKOFF_FACTOR = 1.1f
        private const val POLLING_INTERVAL = 5000L
    }

}