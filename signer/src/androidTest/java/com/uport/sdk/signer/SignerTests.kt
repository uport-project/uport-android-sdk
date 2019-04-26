package com.uport.sdk.signer

import android.content.Context
import android.support.test.InstrumentationRegistry
import com.uport.sdk.signer.encryption.KeyProtection
import me.uport.sdk.core.decodeBase64
import me.uport.sdk.core.decodeJose
import me.uport.sdk.core.getDerEncoded
import me.uport.sdk.core.getJoseEncoded
import me.uport.sdk.core.getUncompressedPublicKeyWithPrefix
import me.uport.sdk.core.padBase64
import me.uport.sdk.core.toBase64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.kethereum.crypto.signMessage
import org.kethereum.crypto.toECKeyPair
import org.kethereum.extensions.hexToBigInteger
import org.kethereum.model.PrivateKey
import org.spongycastle.util.encoders.Hex
import org.walleth.khex.hexToByteArray
import org.walleth.khex.toNoPrefixHexString
import java.math.BigInteger
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class SignerTests {

    private lateinit var context: Context

    @Before
    fun runBeforeEachTest() {
        context = InstrumentationRegistry.getTargetContext()
    }

    @Test
    fun testKeyCreationAndUsage() {
        val latch = CountDownLatch(1)

        val signer = UportSigner()
        signer.createKey(context, KeyProtection.Level.SIMPLE) { err, address, pubKey ->

            assertNull(err)

            assertTrue(address.matches("^0x[0-9a-fA-F]+$".toRegex()))

            val pubKeyBytes = pubKey.decodeBase64()
            assertEquals(65, pubKeyBytes.size)

            UportSigner().signJwtBundle(context, address, "hello".toBase64().padBase64(), "") { _, _ ->
                latch.countDown()
            }
        }

        latch.await(20, TimeUnit.SECONDS)
    }

    @Test
    fun testKeyImportAndUsage() {
        val privKeyBytes = "5047c789919e943c559d8c134091d47b4642122ba0111dfa842ef6edefb48f38".hexToByteArray()
        val latch = CountDownLatch(1)

        val signer = UportSigner()
        signer.saveKey(context, KeyProtection.Level.SIMPLE, privKeyBytes) { err, address, pubKey ->

            assertNull(err)

            assertTrue(address.matches("^0x[0-9a-fA-F]+$".toRegex()))

            val pubKeyBytes = pubKey.decodeBase64()
            assertEquals(65, pubKeyBytes.size)

            UportSigner().signJwtBundle(context, address, "hello".toBase64().padBase64(), "") { _, _ ->
                latch.countDown()
            }
        }

        latch.await(20, TimeUnit.SECONDS)
    }

    @Test
    fun testKeyImportAndUsageMulti() {
        val privKeyBytes = "278a5de700e29faae8e40e366ec5012b5ec63d36ec77e8a2417154cc1d25383f".hexToByteArray()

        val refData1 = "ZXlKMGVYQWlPaUpLVjFRaUxDSmhiR2NpT2lKRlV6STFOa3NpZlEuZXlKcGMzTWlPaUl6TkhkcWMzaDNkbVIxWVc1dk4wNUdRemgxYWs1S2JrWnFZbUZqWjFsbFYwRTRiU0lzSW1saGRDSTZNVFE0TlRNeU1URXpNeXdpWTJ4aGFXMXpJanA3SW01aGJXVWlPaUpDYjJJaWZTd2laWGh3SWpveE5EZzFOREEzTlRNemZR"
        val refSignature1 = "sg1oJ7J_f2pWaX2JwqzA61oWMUK5v0LYVxUp3PvG7Y25CVYWPyQ6UhA7U9d4w3Ny74k7ryMaUz7En5RSL4pyXg".decodeJose(28)

        val refData2 = "ZXlKMGVYQWlPaUpLVjFRaUxDSmhiR2NpT2lKRlV6STFOa3NpZlEuZXlKcGMzTWlPaUl6TkhkcWMzaDNkbVIxWVc1dk4wNUdRemgxYWs1S2JrWnFZbUZqWjFsbFYwRTRiU0lzSW1saGRDSTZNVFE0TlRNeU1URXpNekF3TUN3aVkyeGhhVzF6SWpwN0ltNWhiV1VpT2lKQ2IySWlmU3dpWlhod0lqb3hORGcxTkRBM05UTXpNREF3ZlE="
        val refSignature2 = "XJlwY1KrGRa53oHjz6vjsJadn-Er1ZW6WvLg1KiBQonV9vwqan-hAvn4tNFh7qyZMxxa3xyO7wN7GNuz6_UJ5Q".decodeJose(27)

        val refData3 = "ZXlKMGVYQWlPaUpLVjFRaUxDSmhiR2NpT2lKRlV6STFOa3NpZlEuZXlKcGMzTWlPaUl6TkhkcWMzaDNkbVIxWVc1dk4wNUdRemgxYWs1S2JrWnFZbUZqWjFsbFYwRTRiU0lzSW1saGRDSTZNVFE0TlRNeU1URXpNeXdpWTJ4aGFXMXpJanA3SW01aGJXVWlPaUpDYjJJaWZTd2laWGh3SWpveU5EZzFNekl4TVRNemZR"
        val refSignature3 = "yHkI8fY42iquI-3CSM0k75cJQ4X1DoiGV436YgFQIhJUvjO17q01KhJv2jXviCQrstYe7MZpmJE4SxTvCoC1qQ".decodeJose(27)

        val latch = CountDownLatch(3)

        val signer = UportSigner()
        signer.saveKey(context, KeyProtection.Level.SIMPLE, privKeyBytes) { err, address, _ ->

            assertNull(err)

            assertEquals("0xf3beac30c498d9e26865f34fcaa57dbb935b0d74", address)

            UportSigner().signJwtBundle(context, address, refData1, "") { signerErr, sig ->
                assertNull(signerErr)
                assertEquals(refSignature1, sig)
                latch.countDown()
            }

            UportSigner().signJwtBundle(context, address, refData2, "") { signerErr, sig ->
                assertNull(signerErr)
                assertEquals(refSignature2, sig)
                latch.countDown()
            }

            UportSigner().signJwtBundle(context, address, refData3, "") { signerErr, sig ->
                assertNull(signerErr)
                assertEquals(refSignature3, sig)
                latch.countDown()
            }

        }

        latch.await(20, TimeUnit.SECONDS)
    }

    @Test
    fun testPublicKey1() {
        val referencePrivateKey = "5047c789919e943c559d8c134091d47b4642122ba0111dfa842ef6edefb48f38"
        val referencePublicKey = "04bf42759e6d2a684ef64a8210c55bf2308e4101f78959ffa335ff045ef1e4252b1c09710281f8971b39efed7bfb61ae381ed73b9faa5a96f17e00c1a4c32796b1"
        val keyPair = PrivateKey(referencePrivateKey.hexToBigInteger()).toECKeyPair()
        val pubKeyBytes = keyPair.getUncompressedPublicKeyWithPrefix()
        val pubKeyHex = pubKeyBytes.toNoPrefixHexString()

        assertEquals(referencePublicKey, pubKeyHex)
    }


    @Test
    fun keyImportGeneratesProperPublicKeyAndAddress() {
        val privKeyBytes = "5047c789919e943c559d8c134091d47b4642122ba0111dfa842ef6edefb48f38".hexToByteArray()

        val referencePublicKey = "BL9CdZ5tKmhO9kqCEMVb8jCOQQH3iVn/ozX/BF7x5CUrHAlxAoH4lxs57+17+2GuOB7XO5+qWpbxfgDBpMMnlrE="
        val referenceAddress = "0x45c4EBd7Ffb86891BA6f9F68452F9F0815AAcD8b".toLowerCase()

        val latch = CountDownLatch(1)

        val signer = UportSigner()
        signer.saveKey(context, KeyProtection.Level.SIMPLE, privKeyBytes) { err, address, pubKey ->

            assertNull(err)

            assertEquals(referenceAddress, address)

            assertEquals(referencePublicKey, pubKey)

            latch.countDown()
        }

        latch.await(20, TimeUnit.SECONDS)
    }

    @Test
    fun testPublicKey2() {
        val keypair = PrivateKey("278a5de700e29faae8e40e366ec5012b5ec63d36ec77e8a2417154cc1d25383f".hexToBigInteger()).toECKeyPair()
        val pubKeyBytes = keypair.getUncompressedPublicKeyWithPrefix()
        val pubKeyEnc = Hex.toHexString(pubKeyBytes)
        assertEquals("04fdd57adec3d438ea237fe46b33ee1e016eda6b585c3e27ea66686c2ea535847946393f8145252eea68afe67e287b3ed9b31685ba6c3b00060a73b9b1242d68f7", pubKeyEnc)
    }

    @Test
    fun testJwtJose() {
        val referencePrivateKey = "5047c789919e943c559d8c134091d47b4642122ba0111dfa842ef6edefb48f38"
        val referenceSignature = "N0vw5-xCGN8XN4Q7E8XppJJi0Dch92MhMExZBqPLxJKhYYWxrb2HoY1FQ1YGC011DG1YzzdsbV1_0nIQ0ONKOg"

        val msg = "Hello, world!".toByteArray()

        val keyPair = PrivateKey(referencePrivateKey.hexToBigInteger()).toECKeyPair()

        val sigData = UportSigner().signJwt(msg, keyPair).getJoseEncoded()

        assertEquals(referenceSignature, sigData)
    }

    @Test
    fun testJwtDer() {
        val referencePrivateKey = "5047c789919e943c559d8c134091d47b4642122ba0111dfa842ef6edefb48f38"
        val referenceSignature = "30450220374bf0e7ec4218df1737843b13c5e9a49262d03721f76321304c5906a3cbc492022100a16185b1adbd87a18d454356060b4d750c6d58cf376c6d5d7fd27210d0e34a3a"

        val msg = "Hello, world!".toByteArray()

        val keyPair = PrivateKey(referencePrivateKey.hexToBigInteger()).toECKeyPair()

        val sigData = UportSigner().signJwt(msg, keyPair).getDerEncoded()

        assertEquals(referenceSignature, sigData)
    }

    @Test
    fun testJwtComponents() {
        val referencePrivateKey = Hex.decode("5047c789919e943c559d8c134091d47b4642122ba0111dfa842ef6edefb48f38")

        val referenceR = "374bf0e7ec4218df1737843b13c5e9a49262d03721f76321304c5906a3cbc492".hexToBigInteger()
        val referenceS = "a16185b1adbd87a18d454356060b4d750c6d58cf376c6d5d7fd27210d0e34a3a".hexToBigInteger()

        val msg = "Hello, world!".toByteArray()

        val keyPair = PrivateKey(referencePrivateKey).toECKeyPair()

        val sigData = UportSigner().signJwt(msg, keyPair)

        assertEquals(referenceR, sigData.r)
        assertEquals(referenceS, sigData.s)
    }

    @Test
    fun testSignTxComponents() {
        val referencePrivKeyBytes = "NobiRYkMf5l3Zrc6Idjln2OF4SCIMa84YldHkMvD0Vg=".decodeBase64()

        val referenceR = BigInteger(1, "gJ47XvJfSjsDkTni+3D3C2NuuonHejsB4MccGjbYQSY=".decodeBase64())
        val referenceS = BigInteger(1, "OFJN/NPkEstrw39FlLutEEtnZLsUxk5CxplzAQbRiFo=".decodeBase64())

        val rawTransaction = "84CFC6Q7dACDL+/YlJ4gaMziLeTh6A8Vy3HvQ1ogo7N8iA3gtrOnZAAAiQq83vASNFZ4kA==".decodeBase64()

        val keyPair = PrivateKey(referencePrivKeyBytes).toECKeyPair()

        val sigData = keyPair.signMessage(rawTransaction)

        val obtainedR = sigData.r
        val obtainedS = sigData.s

        assertEquals(referenceR, obtainedR)
        assertEquals(referenceS, obtainedS)
    }

    @Test
    fun testEncStorage() {
        val signer = UportSigner()
        var latch = CountDownLatch(1)

        val label = "whatever"
        val payload = "foobar"

        signer.storeEncryptedPayload(context,
                KeyProtection.Level.SIMPLE,
                label,
                payload.toByteArray()
        ) { err, result ->
            assertNull(err)
            assertTrue(result)
            latch.countDown()
        }
        latch.await(20, TimeUnit.SECONDS)

        latch = CountDownLatch(1)
        signer.loadEncryptedPayload(context,
                label,
                "just decrypt it already"
        ) { err, resultBytes ->
            assertNull(err)
            assertEquals(String(resultBytes), payload)
            latch.countDown()
        }
        latch.await(20, TimeUnit.SECONDS)
    }

    @Test
    fun testAllAddresses() {
        val signer = UportSigner()

        val rand = Random()
        val privKeys = LinkedList<ByteArray>()
        val addresses = LinkedList<String>()

        val iter = 10
        val createLatch = CountDownLatch(iter)

        //create and store a bunch of addresses
        for (i in 0..iter) {
            val pk = ByteArray(32)
            rand.nextBytes(pk)
            privKeys.push(pk)

            signer.saveKey(context,
                    KeyProtection.Level.SIMPLE,
                    pk
            ) { err, address, _ ->
                assertNull(err)
                addresses.push(address)
                createLatch.countDown()
            }
        }

        createLatch.await(20, TimeUnit.SECONDS)

        //check if the addresses are read back successfully from storage
        val readLatch = CountDownLatch(1)
        val storedAddressList = LinkedList<String>()
        signer.allAddresses(context
        ) { list ->
            storedAddressList.addAll(list)
            readLatch.countDown()
        }
        readLatch.await(20, TimeUnit.SECONDS)

        assertTrue(storedAddressList.containsAll(addresses))

    }

    @Test
    fun testDeleteKey() {
        val referencePrivateKey = Hex.decode("5047c789919e943c559d8c134091d47b4642122ba0111dfa842ef6edefb48f38")

        val tested = UportSigner()

        var keyHandle = ""

        var latch = CountDownLatch(1)
        tested.saveKey(context, KeyProtection.Level.SIMPLE, referencePrivateKey) { err, addr, _ ->
            assertNull(err)
            keyHandle = addr
            latch.countDown()
        }
        latch.await(20, TimeUnit.SECONDS)

        tested.deleteKey(context, keyHandle)

        latch = CountDownLatch(1)
        tested.allAddresses(context) { all ->
            assertFalse(all.contains(keyHandle))
            latch.countDown()
        }

        latch.await(20, TimeUnit.SECONDS)
    }
}

