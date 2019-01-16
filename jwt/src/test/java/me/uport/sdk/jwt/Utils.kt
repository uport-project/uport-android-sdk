package me.uport.sdk.jwt

import assertk.AssertBlock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

/**
 * simple wrapper to make assertions on coroutine blocks
 */
fun <T> coAssert(block: suspend CoroutineScope.() -> T): AssertBlock<T> {
    return assertk.assert {
        runBlocking {
            block()
        }
    }
}