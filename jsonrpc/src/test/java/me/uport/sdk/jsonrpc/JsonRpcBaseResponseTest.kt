package me.uport.sdk.jsonrpc


import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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

        assertNotNull(item)
        item!!

        assertEquals("0xdca7ef03e98e0dc2b855be647c39abe984fcf21b", item.address)
        assertEquals(2, item.topics.size)
        assertEquals(BigInteger.ONE, item.transactionIndex)
    }
}