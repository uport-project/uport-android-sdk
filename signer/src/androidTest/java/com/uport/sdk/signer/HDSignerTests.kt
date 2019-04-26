package com.uport.sdk.signer

import android.content.Context
import android.support.test.InstrumentationRegistry
import com.uport.sdk.signer.encryption.KeyProtection.Level.SIMPLE
import com.uport.sdk.signer.testutil.ensureSeedIsImportedInTargetContext
import me.uport.sdk.core.decodeBase64
import me.uport.sdk.core.decodeJose
import me.uport.sdk.core.padBase64
import me.uport.sdk.core.toBase64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.kethereum.bip32.toKey
import org.kethereum.bip39.model.MnemonicWords
import org.kethereum.bip39.toSeed
import org.kethereum.extensions.hexToBigInteger
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.security.Security
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class HDSignerTests {

    init {
        //Kethereum has some provider initialization code that is causing problems if that code is used before any hybrid encryption code
        // the failure shows up as "java.lang.AssertionError: expected null, but was:<java.lang.IllegalStateException: Can't generate certificate>"
        //TODO: check back here when that is fixed: https://github.com/walleth/kethereum/issues/22
        Security.addProvider(BouncyCastleProvider())
    }

    private lateinit var context: Context

    @Before
    fun runBeforeEachTest() {
        context = InstrumentationRegistry.getTargetContext()
    }

    @Test
    fun testSeedCreationAndUsage() {
        val latch = CountDownLatch(1)

        UportHDSigner().createHDSeed(context, SIMPLE) { err, rootAddress, pubKey ->

            assertNull(err)

            assertTrue(rootAddress.matches("^0x[0-9a-fA-F]+$".toRegex()))

            val pubKeyBytes = pubKey.decodeBase64()
            assertEquals(65, pubKeyBytes.size)

            UportHDSigner().signJwtBundle(context, rootAddress, "m/0'", "hello".toBase64().padBase64(), "") { error, _ ->
                assertNull(error)
                latch.countDown()
            }
        }

        latch.await(20, TimeUnit.SECONDS)
    }

    @Test
    fun testSeedImport() {
        val referenceSeedPhrase = "vessel ladder alter error federal sibling chat ability sun glass valve picture"
        val referenceAddress = "0x794adde0672914159c1b77dd06d047904fe96ac8"
        val referencePublicKey = "BFcWkA3uvBb9nSyJmk5rJgx69UtlGN0zwDiNx5TcVmENEUcvF2V26GYP9/3HNE/7vquemm45hDYEqr1/Nph9aIE="

        val latch = CountDownLatch(1)
        UportHDSigner().importHDSeed(context, SIMPLE, referenceSeedPhrase) { err, address, pubKey ->

            assertNull(err)

            assertEquals(referenceAddress, address)

            assertEquals(referencePublicKey, pubKey)

            latch.countDown()
        }

        latch.await(20, TimeUnit.SECONDS)
    }

    //JWT signing something using a derived uPort Root
    @Test
    fun testJwtComponents() {

        val referenceSeed = MnemonicWords("vessel ladder alter error federal sibling chat ability sun glass valve picture").toSeed()
        val referencePayload = "Hello, world!".toByteArray()

        val referencePrivateKey = "65fc670d9351cb87d1f56702fb56a7832ae2aab3427be944ab8c9f2a0ab87960".hexToBigInteger()

        val referenceR = "6bcd81446183af193ca4a172d5c5c26345903b24770d90b5d790f74a9dec1f68".hexToBigInteger()
        val referenceS = "e2b85b3c92c9b4f3cf58de46e7997d8efb6e14b2e532d13dfa22ee02f3a43d5d".hexToBigInteger()

        val derivedRootExtendedKey = referenceSeed.toKey(UportHDSigner.UPORT_ROOT_DERIVATION_PATH)

        assertEquals(referencePrivateKey, derivedRootExtendedKey.keyPair.privateKey.key)

        val keyPair = derivedRootExtendedKey.keyPair

        val sigData = UportSigner().signJwt(referencePayload, keyPair)

        assertEquals(referenceR, sigData.r)
        assertEquals(referenceS, sigData.s)
    }


    @Test
    fun testSeedImportAndUsage() {
        val referenceSeedPhrase = "vessel ladder alter error federal sibling chat ability sun glass valve picture"
        val referenceRootAddress = ensureSeedIsImportedInTargetContext(referenceSeedPhrase)
        val referenceSignature = "lnEso6Io2pJvlC6sWDLRkvxvpXqcUpZpvr4sdpHcTGA66Y1zher8KlrnWzQ2tt_lpxpx2YYdbfdtkfVmwjex2Q".decodeJose(28)

        val referencePayload = "Hello world".toBase64().padBase64()


        val latch = CountDownLatch(1)

        UportHDSigner().signJwtBundle(context, referenceRootAddress, UportHDSigner.UPORT_ROOT_DERIVATION_PATH, referencePayload, "") { error, signature ->
            assertNull(error)
            assertEquals(referenceSignature, signature)

            latch.countDown()
        }

        latch.await(20, TimeUnit.SECONDS)

    }

    @Test
    fun checkShowSeed() {
        val referenceSeedPhrase = "idle giraffe soldier dignity angle tiger false finish busy glow ramp frog"
        val referenceRootAddress = ensureSeedIsImportedInTargetContext(referenceSeedPhrase)

        //check that retrieving it yields the same phrase
        val latch = CountDownLatch(1)
        UportHDSigner().showHDSeed(context, referenceRootAddress, "") { ex, phrase ->
            assertNull(ex)
            assertEquals(referenceSeedPhrase, phrase)
            latch.countDown()
        }
        latch.await(20, TimeUnit.SECONDS)
    }

    @Test
    fun testDeleteSeed() {
        val tested = UportHDSigner()

        val referencePhrase = "vessel ladder alter error federal sibling chat ability sun glass valve picture"
        val refRoot = ensureSeedIsImportedInTargetContext(referencePhrase)

        assertTrue(tested.allHDRoots(context).contains(refRoot))

        tested.deleteSeed(context, refRoot)

        assertFalse(tested.allHDRoots(context).contains(refRoot))
    }

}