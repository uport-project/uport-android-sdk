package me.uport.sdk

import android.content.Context
import me.uport.sdk.core.EthNetwork
import me.uport.sdk.core.IFuelTokenProvider

/**
 * Encapsulates a configuration setup for the uPort SDK
 */
class Configuration {

    lateinit var applicationContext: Context
    lateinit var fuelTokenProvider: IFuelTokenProvider
    var network: EthNetwork? = null

    /**
     * Allows the configuration of a callback that can assign a fuelToken to a device address.
     *
     * This functionality is being phased out.
     */
    @Deprecated("This functionality is being phased out.")
    @Suppress("unused")
    fun setFuelTokenProvider(provider: IFuelTokenProvider): Configuration {
        this.fuelTokenProvider = provider
        return this
    }

    /**
     * Plugs the uPort SDK with an application context.
     */
    fun setApplicationContext(context: Context): Configuration {
        this.applicationContext = context.applicationContext
        return this
    }


    /**
     * Sets the default network to be used for Ethereum interactions
     */
    fun setEthNetwork(network: EthNetwork) {
        this.network = network
    }

}