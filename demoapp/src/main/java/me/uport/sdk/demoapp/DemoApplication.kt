package me.uport.sdk.demoapp

import android.app.Application
import me.uport.sdk.Uport

class DemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val config = Uport.Configuration()
                .setApplicationContext(this)

        Uport.initialize(config)
    }
}