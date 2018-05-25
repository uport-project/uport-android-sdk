package me.uport.sdk

class UportNotInitializedException : RuntimeException(
        "The uport SDK hasn't been initialized." +
                "The best place to call `initialize()` is in" +
                " the `onCreate()` method of your `Application` class") {

}
