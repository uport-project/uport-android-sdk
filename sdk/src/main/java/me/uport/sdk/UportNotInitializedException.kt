package me.uport.sdk

/**
 * Thrown by various methods of the uPort SDK that need an initial configuration to be specified
 */
class UportNotInitializedException : RuntimeException(
        """
        The uport SDK hasn't been initialized.
        The best place to call `initialize()` is in the `onCreate()` method of your `Application` class
        """.trimIndent()
)
