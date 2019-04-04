@file:Suppress("DEPRECATION")

package com.uport.sdk.signer

import android.content.Context
import android.support.test.InstrumentationRegistry
import com.uport.sdk.signer.storage.CryptoUtil
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.*

class CryptoUtilTest {

    private lateinit var context: Context

    @Before
    fun runBeforeEachTest() {
        context = InstrumentationRegistry.getTargetContext()
    }

    @Test
    fun encryptBlobsOfDifferentSize() {

        val textSize = listOf(128, 256, 512, 1024, 2048, 4096, 13, 1234, 6123, 65535)

        textSize.forEach {
            val blob = ByteArray(it)
            Random().nextBytes(blob)

            try {
                println("encrypting message of size $it")
                val encBundle = CryptoUtil(context, "gigel").encrypt(blob)
                println("encrypted message of size $it")
                val decBlob = CryptoUtil(context, "gigel").decrypt(encBundle)
                println("decrypted message of size $it")
                assertArrayEquals(" failed to decrypt blob of size $it:", blob, decBlob)
            } catch (ex: Exception) {
                println("failed at message of size $it")
                throw ex
            }
        }

        assertTrue(true)
    }

}
