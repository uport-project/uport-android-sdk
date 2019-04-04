package com.uport.sdk.signer.encryption

import android.support.test.InstrumentationRegistry
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class SimpleAsymmetricProtectionTest {

    @Before
    fun setUp() {

    }

    @Test
    fun encryptDecryptRandomBlobsOfMultipleSizes() {
        val context = InstrumentationRegistry.getTargetContext()
        SimpleAsymmetricProtection().genKey(context)

        val textSize = listOf(128)//, 256, 512, 1024, 2048, 4096, 13, 1234, 6123)

        textSize.forEach {
            val latch = CountDownLatch(1)
            val blob = ByteArray(it)
            Random().nextBytes(blob)
            SimpleAsymmetricProtection().encrypt(context, "", blob) { eerr, ciphertext ->

                assertNull("failed to encrypt a blob of $it bytes", eerr)

                SimpleAsymmetricProtection().decrypt(context, "", ciphertext) { derr, decrypted ->

                    assertNull("failed to decrypt a blob of $it bytes", derr)

                    assertArrayEquals(blob, decrypted)

                    latch.countDown()
                }
            }

            latch.await(20, TimeUnit.SECONDS)
        }
    }
}