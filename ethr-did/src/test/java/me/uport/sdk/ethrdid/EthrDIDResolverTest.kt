@file:Suppress("UnnecessaryVariable")

package me.uport.sdk.ethrdid

import kotlinx.coroutines.experimental.runBlocking
import kotlinx.serialization.json.JSON
import me.uport.sdk.core.Networks
import me.uport.sdk.core.hexToBytes32
import me.uport.sdk.core.utf8
import me.uport.sdk.ethrdid.EthereumDIDRegistry.Events.DIDOwnerChanged
import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.jsonrpc.experimental.getLogs
import org.junit.Assert.*
import org.junit.Test
import org.kethereum.extensions.hexToBigInteger
import pm.gnosis.model.Solidity
import pm.gnosis.utils.hexToByteArray
import java.math.BigInteger


class EthrDIDResolverTest {

    @Test
    fun `last change is blank for new address`() = runBlocking {
        val rpc = JsonRPC(Networks.rinkeby.rpcUrl)
        val imaginaryAddress = "0x1234"
        val lastChanged = EthrDIDResolver(rpc).lastChanged(imaginaryAddress)
        assertEquals(BigInteger.ZERO, lastChanged.hexToBigInteger())
    }

    @Test
    fun `last change is non-zero for real address with changed owner`() = runBlocking {
        val rpc = JsonRPC(Networks.rinkeby.rpcUrl)
        val realAddress = "0xf3beac30c498d9e26865f34fcaa57dbb935b0d74"
        val lastChanged = EthrDIDResolver(rpc).lastChanged(realAddress)
        println(lastChanged)
        assertNotEquals(BigInteger.ZERO, lastChanged.hexToBigInteger())
    }


    @Test
    fun `real address with activity has logs`() = runBlocking {
        val rpc = JsonRPC(Networks.rinkeby.rpcUrl)
        val realAddress = "0xf3beac30c498d9e26865f34fcaa57dbb935b0d74"
        val resolver = EthrDIDResolver(rpc)
        val lastChanged = resolver.lastChanged(realAddress).hexToBigInteger()
        val logResponse = rpc.getLogs(resolver.registryAddress, listOf(null, realAddress.hexToBytes32()), lastChanged, lastChanged)
        assertNotNull(logResponse)
        assertTrue(logResponse.isNotEmpty())
    }

    @Test
    fun `can parse owner changed logs`() = runBlocking {
        val rpc = JsonRPC(Networks.rinkeby.rpcUrl)
        val realAddress = "0xf3beac30c498d9e26865f34fcaa57dbb935b0d74"
        val resolver = EthrDIDResolver(rpc)
        val lastChanged = 2784036L.toBigInteger()
        val logs = rpc.getLogs(resolver.registryAddress, listOf(null, realAddress.hexToBytes32()), lastChanged, lastChanged)

        assertTrue(logs.isNotEmpty())

        //topics should be 0x prefixed hex strings
        val topics: List<String> = logs[0].topics
        val data: String = logs[0].data
        val args: DIDOwnerChanged.Arguments = DIDOwnerChanged.decode(topics, data)
        //no assertion about args but it should not crash
        val previousBlock = args.previouschange.value

        assertTrue(previousBlock > BigInteger.ZERO)
    }

    @Test
    fun `can parse multiple event logs`() = runBlocking {
        val rpc = JsonRPC(Networks.rinkeby.rpcUrl)
        val realAddress = "0xf3beac30c498d9e26865f34fcaa57dbb935b0d74"
        val resolver = EthrDIDResolver(rpc)
        val events = resolver.getHistory(realAddress)
        println(events)
        assertTrue(events.isNotEmpty())
    }

    // "did/pub/(Secp256k1|Rsa|Ed25519)/(veriKey|sigAuth)/(hex|base64)",
    private val attributeRegexes = listOf(
            "did/pub/Secp256k1/veriKey/hex",
            "did/pub/Rsa/veriKey/hex",
            "did/pub/Ed25519/veriKey/hex",
            "did/pub/Secp256k1/sigAuth/hex",
            "did/pub/Rsa/sigAuth/hex",
            "did/pub/Ed25519/sigAuth/hex",
            "did/pub/Secp256k1/veriKey/base64",
            "did/pub/Rsa/veriKey/base64",
            "did/pub/Ed25519/veriKey/base64",
            "did/pub/Secp256k1/sigAuth/base64",
            "did/pub/Rsa/sigAuth/base64",
            "did/pub/Ed25519/sigAuth/base64",
            "did/pub/Secp256k1/veriKey",
            "did/pub/Rsa/veriKey",
            "did/pub/Ed25519/veriKey",
            "did/pub/Secp256k1/sigAuth",
            "did/pub/Rsa/sigAuth",
            "did/pub/Ed25519/sigAuth",
            "did/pub/Secp256k1",
            "did/pub/Rsa",
            "did/pub/Ed25519"
    )

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun `can parse attribute regex`() {
        val regex = "^did/(pub|auth|svc)/(\\w+)(/(\\w+))?(/(\\w+))?$".toRegex()
        attributeRegexes.forEach {
            val matchResult = regex.find(it)
            assertNotNull("expected to match \"$it\"", matchResult)
            val (section, algo, _, rawType, _, encoding) = matchResult!!.destructured

            assertTrue(section.isNotBlank())
            assertTrue(algo.isNotBlank())
        }
    }

    @Test
    fun `can parse sample attr change event`() {
        val soon = System.currentTimeMillis() / 1000 + 600
        val identity = "0xf3beac30c498d9e26865f34fcaa57dbb935b0d74"
        val owner = identity

        val event = EthereumDIDRegistry.Events.DIDAttributeChanged.Arguments(
                identity = Solidity.Address(identity.hexToBigInteger()),
                name = Solidity.Bytes32("did/pub/Secp256k1/veriKey/base64".toByteArray()),
                value = Solidity.Bytes("0x02b97c30de767f084ce3080168ee293053ba33b235d7116a3263d29f1450936b71".hexToByteArray()),
                validto = Solidity.UInt256(soon.toBigInteger()),
                previouschange = Solidity.UInt256(BigInteger.ZERO)
        )

        val rpc = JsonRPC(Networks.rinkeby.rpcUrl)

        val ddo = EthrDIDResolver(rpc).wrapDidDocument("did:ethr:$identity", owner, listOf(event))
        println(ddo)

        //did not crash
        assertTrue(true)
    }

    @Test
    fun `to and from solidity bytes`() {
        val str = "did/pub/Secp256k1/veriKey/hex"
        val sol = Solidity.Bytes32(str.toByteArray())

//        //this fails. for some reason, the default is resolving to Object.toString() instead of ByteArray.toString()
//        val decodedStr = sol.bytes.toString()

        //this should work no matter what
        val decodedStr = sol.bytes.toString(utf8)
        assertEquals(str, decodedStr)
    }

    @Test
    fun `can resolve real address`() = runBlocking {

        //language=JSON
        val referenceDDOString = """
            {
              "@context": "https://w3id.org/did/v1",
              "id": "did:ethr:0xb9c5714089478a327f09197987f16f9e5d936e8a",
              "publicKey": [{
                   "id": "did:ethr:0xb9c5714089478a327f09197987f16f9e5d936e8a#owner",
                   "type": "Secp256k1VerificationKey2018",
                   "owner": "did:ethr:0xb9c5714089478a327f09197987f16f9e5d936e8a",
                   "ethereumAddress": "0xb9c5714089478a327f09197987f16f9e5d936e8a"}],
              "authentication": [{
                   "type": "Secp256k1SignatureAuthentication2018",
                   "publicKey": "did:ethr:0xb9c5714089478a327f09197987f16f9e5d936e8a#owner"}]
            }
        """.trimIndent()
        val referenceDDO = JSON.nonstrict.parse<DDO>(referenceDDOString)


        val realAddress = "0xb9c5714089478a327f09197987f16f9e5d936e8a"
        val rpc = JsonRPC(Networks.rinkeby.rpcUrl)
        val resolver = EthrDIDResolver(rpc)
        val ddo = resolver.resolve(realAddress)
        assertEquals(referenceDDO, ddo)
        println(ddo)
    }

    @Test
    fun `can normalize DID`() {

        val validDids = listOf(
                "0xb9c5714089478a327f09197987f16f9e5d936e8a",
                "0xB9C5714089478a327F09197987f16f9E5d936E8a",
                "did:ethr:0xb9c5714089478a327f09197987f16f9e5d936e8a",
                "did:ethr:0xB9C5714089478a327F09197987f16f9E5d936E8a"
        )

        val invalidDids = listOf(
                "0xb9c5714089478a327f09197987f16f9e5d936e",
                "B9C5714089478a327F09197987f16f9E5d936E8a",
                "ethr:0xb9c5714089478a327f09197987f16f9e5d936e8a",
                "B9C5714089478a327F09197987f16f9E5d936E8a",
                "B9C5714089478a327F09197987f16f9E5d936E"
        )

        validDids.forEach {
            val normalizedDid = EthrDIDResolver.normalizeDid(it)
            assertTrue("failed to normalize $it", normalizedDid.isNotBlank())
            assertEquals("did:ethr:0xb9c5714089478a327f09197987f16f9e5d936e8a", normalizedDid.toLowerCase())
        }

        invalidDids.forEach {
            val normalizedDid = EthrDIDResolver.normalizeDid(it)
            assertTrue("should have failed to normalize $it but got $normalizedDid", normalizedDid.isBlank())
        }
    }

}