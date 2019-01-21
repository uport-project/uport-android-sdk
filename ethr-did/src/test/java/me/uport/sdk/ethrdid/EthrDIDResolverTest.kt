@file:Suppress("UnnecessaryVariable")

package me.uport.sdk.ethrdid

import assertk.all
import assertk.assert
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JSON
import me.uport.sdk.core.HttpClient
import me.uport.sdk.core.Networks
import me.uport.sdk.core.hexToBytes32
import me.uport.sdk.core.utf8
import me.uport.sdk.ethrdid.EthereumDIDRegistry.Events.DIDOwnerChanged
import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.jsonrpc.JsonRpcLogItem
import org.junit.Test
import org.kethereum.extensions.hexToBigInteger
import pm.gnosis.model.Solidity
import pm.gnosis.utils.hexToByteArray
import java.math.BigInteger


class EthrDIDResolverTest {

    @Test
    fun `last change is blank for new address`() = runBlocking {
        val rpc = spyk(JsonRPC(Networks.rinkeby.rpcUrl))
        val encodedCallSlot = slot<String>()

        coEvery { rpc.ethCall(any(), capture(encodedCallSlot)) }
                //language=json
                .returns("""{"jsonrpc":"2.0","id":1,"result":"0x0000000000000000000000000000000000000000000000000000000000000000"}""")

        val imaginaryAddress = "0x1234"
        val lastChanged = EthrDIDResolver(rpc).lastChanged(imaginaryAddress)

        assert(encodedCallSlot.captured).isEqualTo("0xf96d0f9f0000000000000000000000000000000000000000000000000000000000001234")
        assert(lastChanged.hexToBigInteger()).isEqualTo(BigInteger.ZERO)
    }

    @Test
    fun `last change is non-zero for real address with changed owner`() = runBlocking {
        val rpc = spyk(JsonRPC(Networks.rinkeby.rpcUrl))
        val encodedCallSlot = slot<String>()
        coEvery { rpc.ethCall(any(), capture(encodedCallSlot)) }
                //language=json
                .returns("""{"jsonrpc":"2.0","id":1,"result":"0x00000000000000000000000000000000000000000000000000000000002a8a7d"}""")

        val realAddress = "0xf3beac30c498d9e26865f34fcaa57dbb935b0d74"
        val lastChanged = EthrDIDResolver(rpc).lastChanged(realAddress)

        assert(encodedCallSlot.captured).isEqualTo("0xf96d0f9f000000000000000000000000f3beac30c498d9e26865f34fcaa57dbb935b0d74")
        assert(lastChanged.hexToBigInteger()).isNotEqualTo(BigInteger.ZERO)
    }


    @Test
    fun `can parse getLogs response`() = runBlocking {
        val http = mockk<HttpClient>()
        val rpc = JsonRPC(Networks.rinkeby.rpcUrl, http)
        val realAddress = "0xf3beac30c498d9e26865f34fcaa57dbb935b0d74"
        val resolver = EthrDIDResolver(rpc)
        val lastChanged = "0x00000000000000000000000000000000000000000000000000000000002a8a7d".hexToBigInteger()

        //language=json
        val cannedLogsResponse = """{"jsonrpc":"2.0","id":1,"result":[{"address":"0xdca7ef03e98e0dc2b855be647c39abe984fcf21b","blockHash":"0x10b9345e8c8ba8f5fbd164fc104e4959abb010ddcc38b164ac1c62c55e75856e","blockNumber":"0x2a8a7d","data":"0x536563703235366b31566572696669636174696f6e4b6579323031380000000000000000000000000000000045c4ebd7ffb86891ba6f9f68452f9f0815aacd8b0000000000000000000000000000000000000000000000000000000117656a2f00000000000000000000000000000000000000000000000000000000002a7b24","logIndex":"0x16","removed":false,"topics":["0x5a5084339536bcab65f20799fcc58724588145ca054bd2be626174b27ba156f7","0x000000000000000000000000f3beac30c498d9e26865f34fcaa57dbb935b0d74"],"transactionHash":"0x59180d9f3257a538ef77ba7363ec55ed76b609bf0c90cdf7fb710d695ebaa5c0","transactionIndex":"0x17"}]}"""
        coEvery { http.urlPost(any(), any(), any()) } returns cannedLogsResponse

        val logResponse = rpc.getLogs(resolver.registryAddress, listOf(null, realAddress.hexToBytes32()), lastChanged, lastChanged)

        assert(logResponse).all {
            isNotNull()
            isNotEmpty()
        }
    }

    @Test
    fun `can parse owner changed logs`() = runBlocking {
        val logItem = JsonRpcLogItem(
                address = "0xdca7ef03e98e0dc2b855be647c39abe984fcf21b",
                topics = listOf("0x38a5a6e68f30ed1ab45860a4afb34bcb2fc00f22ca462d249b8a8d40cda6f7a3",
                        "0x000000000000000000000000f3beac30c498d9e26865f34fcaa57dbb935b0d74"),
                data = "0x000000000000000000000000f3beac30c498d9e26865f34fcaa57dbb935b0d74000000000000000000000000000000000000000000000000000000000029db37",
                blockNumber = BigInteger("2784036"),
                transactionHash = "0xb42e3fbf29fffe53746021837396cf1a2e9ad88a82d5c9213e2725b5e72e123e",
                transactionIndex = BigInteger("17"),
                blockHash = "0xf7b8a4b602e6e47fc190ecbb213d09cd577186b3d2f28a0816eff6da55a6e469",
                logIndex = BigInteger("20"),
                removed = false)

        val topics: List<String> = logItem.topics
        val data: String = logItem.data
        val args: DIDOwnerChanged.Arguments = DIDOwnerChanged.decode(topics, data)
        //no assertion about args but it should not crash
        val previousBlock = args.previouschange.value

        assert(previousBlock).isGreaterThan(BigInteger.ZERO)
    }

    @Test
    fun `can parse multiple event logs`() = runBlocking {
        val realAddress = "0xf3beac30c498d9e26865f34fcaa57dbb935b0d74"

        val rpc = spyk(JsonRPC(Networks.rinkeby.rpcUrl))
        coEvery { rpc.ethCall(any(), eq("0xf96d0f9f000000000000000000000000f3beac30c498d9e26865f34fcaa57dbb935b0d74")) }
                //language=json
                .returns("""{"jsonrpc":"2.0","id":1,"result":"0x00000000000000000000000000000000000000000000000000000000002a8a7d"}""")
        val cannedResponses: List<List<JsonRpcLogItem>> = listOf(
                listOf(JsonRpcLogItem(address = "0xdca7ef03e98e0dc2b855be647c39abe984fcf21b", blockHash = "0x10b9345e8c8ba8f5fbd164fc104e4959abb010ddcc38b164ac1c62c55e75856e", blockNumber = "0x2a8a7d".hexToBigInteger(), data = "0x536563703235366b31566572696669636174696f6e4b6579323031380000000000000000000000000000000045c4ebd7ffb86891ba6f9f68452f9f0815aacd8b0000000000000000000000000000000000000000000000000000000117656a2f00000000000000000000000000000000000000000000000000000000002a7b24", logIndex = "0x16".hexToBigInteger(), removed = false, topics = listOf("0x5a5084339536bcab65f20799fcc58724588145ca054bd2be626174b27ba156f7", "0x000000000000000000000000f3beac30c498d9e26865f34fcaa57dbb935b0d74"), transactionHash = "0x59180d9f3257a538ef77ba7363ec55ed76b609bf0c90cdf7fb710d695ebaa5c0", transactionIndex = "0x17".hexToBigInteger())),
                listOf(JsonRpcLogItem(address = "0xdca7ef03e98e0dc2b855be647c39abe984fcf21b", blockHash = "0xf7b8a4b602e6e47fc190ecbb213d09cd577186b3d2f28a0816eff6da55a6e469", blockNumber = "0x2a7b24".hexToBigInteger(), data = "0x000000000000000000000000f3beac30c498d9e26865f34fcaa57dbb935b0d74000000000000000000000000000000000000000000000000000000000029db37", logIndex = "0x14".hexToBigInteger(), removed = false, topics = listOf("0x38a5a6e68f30ed1ab45860a4afb34bcb2fc00f22ca462d249b8a8d40cda6f7a3", "0x000000000000000000000000f3beac30c498d9e26865f34fcaa57dbb935b0d74"), transactionHash = "0xb42e3fbf29fffe53746021837396cf1a2e9ad88a82d5c9213e2725b5e72e123e", transactionIndex = "0x11".hexToBigInteger())),
                listOf(JsonRpcLogItem(address = "0xdca7ef03e98e0dc2b855be647c39abe984fcf21b", blockHash = "0xf0bfb1aaa47ce10e6aa99940bafc2bb11f3de742d44d2288b4546250e67b0971", blockNumber = "0x29db37".hexToBigInteger(), data = "0x00000000000000000000000045c4ebd7ffb86891ba6f9f68452f9f0815aacd8b0000000000000000000000000000000000000000000000000000000000000000", logIndex = "0x0".hexToBigInteger(), removed = false, topics = listOf("0x38a5a6e68f30ed1ab45860a4afb34bcb2fc00f22ca462d249b8a8d40cda6f7a3", "0x000000000000000000000000f3beac30c498d9e26865f34fcaa57dbb935b0d74"), transactionHash = "0x70829b62ac232269a0524b180054532eff18b5fbc60b7102b6120844b5cdb1d8", transactionIndex = "0x1".hexToBigInteger())),
                emptyList()
        )
        coEvery { rpc.getLogs(any(), any(), any(), any()) }.returnsMany(cannedResponses)

        val resolver = EthrDIDResolver(rpc)
        val events = resolver.getHistory(realAddress)
        assert(events).hasSize(3)
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

            assert(matchResult).isNotNull()

            val (section, algo, _, rawType, _, encoding) = matchResult!!.destructured

            assert(section).isNotEmpty()
            assert(algo).isNotEmpty()
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

        assert {
            EthrDIDResolver(rpc).wrapDidDocument("did:ethr:$identity", owner, listOf(event))
        }.doesNotThrowAnyException()
    }

    @Test
    fun `to and from solidity bytes`() {
        val str = "did/pub/Secp256k1/veriKey/hex"
        val sol = Solidity.Bytes32(str.toByteArray())
        val decodedStr = sol.bytes.toString(utf8)
        assert(decodedStr).isEqualTo(str)
    }

    @Test
    fun `can resolve real did`() = runBlocking {
        val http = mockk<HttpClient>()

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
        val referenceDDO = JSON.nonstrict.parse(EthrDIDDocument.serializer(), referenceDDOString)

        val addressHex = "b9c5714089478a327f09197987f16f9e5d936e8a"

        val rpc = spyk(JsonRPC(Networks.rinkeby.rpcUrl, http))
        //canned response for get owner query
        coEvery { rpc.ethCall(any(), eq("0x8733d4e8000000000000000000000000$addressHex")) } returns """{"jsonrpc":"2.0","id":1,"result":"0x000000000000000000000000$addressHex"}"""
        //canned response for last changed query
        coEvery { rpc.ethCall(any(), eq("0xf96d0f9f000000000000000000000000$addressHex")) } returns """{"jsonrpc":"2.0","id":1,"result":"0x0000000000000000000000000000000000000000000000000000000000000000"}"""
        //canned response for getLogs
        coEvery { http.urlPost(any(), any(), any()) } returns """{"jsonrpc":"2.0","id":1,"result":[]}"""

        val resolver = EthrDIDResolver(rpc)
        val ddo = resolver.resolve("did:ethr:0x$addressHex")
        assert(ddo).isEqualTo(referenceDDO)
    }

    @Test
    fun `can normalize DID`() {

        val validDids = listOf(
                "0xb9c5714089478a327f09197987f16f9e5d936e8a",
                "0xB9C5714089478a327F09197987f16f9E5d936E8a",
                "did:ethr:0xb9c5714089478a327f09197987f16f9e5d936e8a",
                "did:ethr:0xB9C5714089478a327F09197987f16f9E5d936E8a",
                "did:ethr:0xB9C5714089478a327F09197987f16f9E5d936E8a#owner"
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
            assert(normalizedDid.toLowerCase()).isEqualTo("did:ethr:0xb9c5714089478a327f09197987f16f9e5d936e8a")
        }

        invalidDids.forEach {
            val normalizedDid = EthrDIDResolver.normalizeDid(it)
            assert(normalizedDid).isEmpty()
        }
    }

}
