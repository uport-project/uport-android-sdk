package me.uport.sdk.core

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface IFuelTokenProvider {
    fun onCreateFuelToken(deviceAddress: String, callback: (err: Exception?, fuelToken: String) -> Unit)
}



suspend fun IFuelTokenProvider.onCreateFuelToken(deviceAddress: String): String = suspendCoroutine {
    this.onCreateFuelToken(deviceAddress) { err, fuelToken ->
        if (err != null) {
            it.resumeWithException(err)
        } else {
            it.resume(fuelToken)
        }
    }
}
