package com.uport.sdk.signer

import android.support.test.InstrumentationRegistry
import com.uport.sdk.signer.UportSigner.Companion.ERR_ACTIVITY_DOES_NOT_EXIST
import com.uport.sdk.signer.encryption.KeyProtection
import com.uport.sdk.signer.testutil.ensureKeyIsImportedInTargetContext
import com.uport.sdk.signer.testutil.ensureSeedIsImportedInTargetContext
import me.uport.sdk.core.padBase64
import me.uport.sdk.core.toBase64
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.kethereum.bip39.generateMnemonic
import org.kethereum.bip39.wordlists.WORDLIST_ENGLISH
import java.security.SecureRandom
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class UserInteractionContextTests {

    private val phrase = generateMnemonic(wordList = WORDLIST_ENGLISH)
    private val key = ByteArray(32).apply { SecureRandom().nextBytes(this) }

    private val context = InstrumentationRegistry.getTargetContext()
    //import a key that needs user authentication
    private val seedHandle = ensureSeedIsImportedInTargetContext(phrase, KeyProtection.Level.PROMPT)
    private val keyHandle = ensureKeyIsImportedInTargetContext(key, KeyProtection.Level.PROMPT)

    @Test
    fun shouldThrowOnShowSeedWhenUsingActivityDependentKey() {

        val latch = CountDownLatch(1)
        UportHDSigner().showHDSeed(context, seedHandle, "this is shown to the user") { err, _ ->

            assertNotNull(err!!)
            assertTrue(err.message?.contains(ERR_ACTIVITY_DOES_NOT_EXIST) ?: false)

            latch.countDown()
        }

        latch.await(20, TimeUnit.SECONDS)
    }

    @Test
    fun shouldThrowOnSignJwtWhenUsingActivityDependentKey() {

        val somePayloadData = "payload to be signed".toByteArray()
        val payload = somePayloadData.toBase64().padBase64()

        val latch = CountDownLatch(1)
        UportHDSigner().signJwtBundle(context, seedHandle, UportHDSigner.UPORT_ROOT_DERIVATION_PATH, payload, "this is shown to the user") { err, _ ->

            assertNotNull(err!!)
            assertTrue(err.message?.contains(ERR_ACTIVITY_DOES_NOT_EXIST) ?: false)

            latch.countDown()
        }

        latch.await(20, TimeUnit.SECONDS)
    }

    @Test
    fun shouldThrowOnSignTxWhenUsingActivityDependentKey() {

        val somePayloadData = "payload to be signed".toByteArray()
        val payload = somePayloadData.toBase64().padBase64()

        val latch = CountDownLatch(1)
        UportHDSigner().signTransaction(context, seedHandle, UportHDSigner.UPORT_ROOT_DERIVATION_PATH, payload, "this is shown to the user") { err, _ ->

            assertNotNull(err!!)
            assertTrue(err.message?.contains(ERR_ACTIVITY_DOES_NOT_EXIST) ?: false)

            latch.countDown()
        }

        latch.await(20, TimeUnit.SECONDS)
    }


    @Test
    fun shouldThrowOnSignJwtSimpleWhenUsingActivityDependentKey() {

        val somePayloadData = "payload to be signed".toByteArray()
        val payload = somePayloadData.toBase64().padBase64()

        val latch = CountDownLatch(1)
        UportSigner().signJwtBundle(context, keyHandle, payload, "this is shown to the user") { err, _ ->

            assertNotNull(err!!)
            assertTrue(err.message?.contains(ERR_ACTIVITY_DOES_NOT_EXIST) ?: false)

            latch.countDown()
        }

        latch.await(20, TimeUnit.SECONDS)
    }

    @Test
    fun shouldThrowOnSignTxSimpleWhenUsingActivityDependentKey() {

        val somePayloadData = "payload to be signed".toByteArray()
        val payload = somePayloadData.toBase64().padBase64()

        val latch = CountDownLatch(1)
        UportSigner().signTransaction(context, keyHandle, payload, "this is shown to the user") { err, _ ->

            assertNotNull(err!!)
            assertTrue(err.message?.contains(ERR_ACTIVITY_DOES_NOT_EXIST) ?: false)

            latch.countDown()
        }

        latch.await(20, TimeUnit.SECONDS)
    }

}