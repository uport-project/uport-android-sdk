package me.uport.sdk.core

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Provides a fuel token given a device address. The fuel token is used in a meta-transaction context
 */
interface IFuelTokenProvider {
    fun onCreateFuelToken(deviceAddress: String, callback: (err: Exception?, fuelToken: String) -> Unit)
}

/**
 * suspend wrapper for using a fuel token provider in coroutine contexts
 */
suspend fun IFuelTokenProvider.onCreateFuelToken(deviceAddress: String): String = suspendCoroutine {
    this.onCreateFuelToken(deviceAddress) { err, fuelToken ->
        if (err != null) {
            it.resumeWithException(err)
        } else {
            it.resume(fuelToken)
        }
    }
}
