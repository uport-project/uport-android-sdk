package me.uport.sdk.demoapp

import android.app.Application
import me.uport.sdk.Configuration
import me.uport.sdk.Uport

/**
 * Use the Application class to initialize the uPort SDK
 */
class DemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val config = Configuration()
                .setApplicationContext(this)

        Uport.initialize(config)
    }
}