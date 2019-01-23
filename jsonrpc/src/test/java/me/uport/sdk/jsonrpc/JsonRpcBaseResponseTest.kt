package me.uport.sdk.jsonrpc

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import me.uport.sdk.core.HttpClient
import me.uport.sdk.core.Networks
import org.junit.Test
import java.math.BigInteger

class JsonRpcBaseResponseTest {

    //language=JSON
    private val logItemJson = """
{
    "address":"0xdca7ef03e98e0dc2b855be647c39abe984fcf21b",
    "topics": [
        "0x38a5a6e68f30ed1ab45860a4afb34bcb2fc00f22ca462d249b8a8d40cda6f7a3",
        "0x000000000000000000000000f3beac30c498d9e26865f34fcaa57dbb935b0d74"
    ],
    "data": "0x00000000000000000000000045c4ebd7ffb86891ba6f9f68452f9f0815aacd8b0000000000000000000000000000000000000000000000000000000000000000",
    "blockNumber":"0x29db37",
    "transactionHash":"0x70829b62ac232269a0524b180054532eff18b5fbc60b7102b6120844b5cdb1d8",
    "transactionIndex":"0x1",
    "blockHash":"0xf0bfb1aaa47ce10e6aa99940bafc2bb11f3de742d44d2288b4546250e67b0971",
    "logIndex":"0x0",
    "removed":false
}
""".trimIndent()

    @Test
    fun `can deserialize log item json`() {
        val adapter = moshi.adapter<JsonRpcLogItem>(JsonRpcLogItem::class.java)
        val item = adapter.fromJson(logItemJson)


        assert(item).isNotNull()
        item!!

        assert(item.address).isEqualTo("0xdca7ef03e98e0dc2b855be647c39abe984fcf21b")
        assert(item.topics.size).isEqualTo(2)
        assert(item.transactionIndex).isEqualTo(BigInteger.ONE)
    }

    @Test
    fun `can parse transaction receipt`() = runBlocking {
        val http = mockk<HttpClient> {
            //language=json
            coEvery { urlPost(any(), any()) } returns """{"jsonrpc":"2.0","id":1,"result":{"blockHash":"0x5619e5036d2288fbda3298be5d9003f2622993465945ad4d25377dd9f6c15646","blockNumber":"0x38ecec","contractAddress":null,"cumulativeGasUsed":"0x14510c","from":"0xbe1085bc3e0812f3df63deced87e29b3bc2db524","gasUsed":"0x1230f","logs":[{"address":"0x40af244c94e679aebf897512720a41d843954a29","blockHash":"0x5619e5036d2288fbda3298be5d9003f2622993465945ad4d25377dd9f6c15646","blockNumber":"0x38ecec","data":"0x00000000000000000000000000000000000000000000000000000000549adbf85915ab705f44b3c43ecc0c2d34834a863132cc200c23016acd57d47b8aa8cf0d000000000000000000000000000000000000000000000000000000005c45f692","logIndex":"0xa","removed":false,"topics":["0x6d91cd6ccac8368394df514e6aee19a55264f5ab49a891af91ca86da27bedd4f"],"transactionHash":"0x1dab51a1127c0294359193b1e0b7f2c8e414efe7e9a85dee59fca21d9b179dcb","transactionIndex":"0x7"}],"logsBloom":"0x00000000000000000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000010000000000000000000000000000000000000000000080000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000080000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000080000000000","status":"0x1","to":"0x40af244c94e679aebf897512720a41d843954a29","transactionHash":"0x1dab51a1127c0294359193b1e0b7f2c8e414efe7e9a85dee59fca21d9b179dcb","transactionIndex":"0x7"}}"""
        }

        val tested = JsonRPC(Networks.rinkeby.rpcUrl, http)

        val receipt = tested.getTransactionReceipt("0x1dab51a1127c0294359193b1e0b7f2c8e414efe7e9a85dee59fca21d9b179dcb")
        assert(receipt).isNotNull()
    }
}