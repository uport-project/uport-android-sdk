package me.uport.sdk.core

interface IFuelTokenProvider {
    suspend fun onCreateFuelToken(deviceAddress: String): String
}
