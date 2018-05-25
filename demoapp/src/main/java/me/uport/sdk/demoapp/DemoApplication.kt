package me.uport.sdk.demoapp

import android.app.Application
import me.uport.sdk.Uport
import me.uport.sdk.fuelingservice.FuelTokenProvider

class DemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val config = Uport.Configuration()
                .setApplicationContext(this)
                .setFuelTokenProvider(
                        FuelTokenProvider(this, "2p1yWKU8Ucd4vuHmYmc3fvcvTkYL11KXdjH"))

        Uport.initialize(config)
    }
}