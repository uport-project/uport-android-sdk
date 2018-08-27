package com.uport.sdk.signer.testutil

import android.support.test.InstrumentationRegistry
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.UportSigner
import com.uport.sdk.signer.encryption.KeyProtection
import org.junit.Assert
import java.util.concurrent.CountDownLatch

/**
 * synchronously imports a given seed phrase at the desired protection level
 */
fun ensureSeedIsImportedInTargetContext(phrase: String, level: KeyProtection.Level = KeyProtection.Level.SIMPLE): String {
    val targetContext = InstrumentationRegistry.getTargetContext()
    val latch = CountDownLatch(1)
    lateinit var handle : String
    UportHDSigner().importHDSeed(targetContext, level, phrase) { err, rootAddress, _ ->
        Assert.assertNull(err)
        handle = rootAddress
        latch.countDown()
    }
    latch.await()
    return handle
}

/**
 * synchronously imports a given private key at the desired protection level
 */
fun ensureKeyIsImportedInTargetContext(key: ByteArray, level: KeyProtection.Level = KeyProtection.Level.SIMPLE): String {
    val targetContext = InstrumentationRegistry.getTargetContext()
    val latch = CountDownLatch(1)
    lateinit var handle : String
    UportSigner().saveKey(targetContext, level, key) { err, rootAddress, _ ->
        Assert.assertNull(err)
        handle = rootAddress
        latch.countDown()
    }
    latch.await()
    return handle
}