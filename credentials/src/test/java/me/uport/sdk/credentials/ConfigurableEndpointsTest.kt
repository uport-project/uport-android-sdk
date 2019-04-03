package me.uport.sdk.credentials

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import me.uport.sdk.core.HttpClient
import me.uport.sdk.ethrdid.EthrDIDResolver
import me.uport.sdk.jsonrpc.JsonRPC
import me.uport.sdk.jwt.JWTTools
import me.uport.sdk.testhelpers.TestTimeProvider
import me.uport.sdk.universaldid.UniversalDID
import me.uport.sdk.uportdid.UportDIDResolver
import org.junit.Test

class ConfigurableEndpointsTest {

    @Test
    fun `can override JsonRPC endpoint for uport did resolver`() = runBlocking {

        val replacementURL = "http://127.0.0.1:8545"

        val httpSpy = spyk(HttpClient())
        coEvery { httpSpy.urlPost(any(), any()) } returns
                """{"jsonrpc":"2.0","id":1,"result":"0x807a7cb8b670125774d70cf94d35e2355bb18bb51cf604f376c9996057f92fbf"}"""
        coEvery { httpSpy.urlGet(any()) } returns
                """{"@context":"http://schema.org","@type":"Person","publicKey":"0x04e8989d1826cd6258906cfaa71126e2db675eaef47ddeb9310ee10db69b339ab960649e1934dc1e1eac1a193a94bd7dc5542befc5f7339845265ea839b9cbe56f","publicEncKey":"k8q5G4YoIMP7zvqMC9q84i7xUBins6dXGt8g5H007F0="}"""

        val rpc = JsonRPC(replacementURL, httpSpy)
        val resolver = UportDIDResolver(rpc)

        resolver.resolve("did:uport:2ozs2ntCXceKkAQKX4c9xp2zPS8pvkJhVqC")

        coVerify {
            httpSpy.urlPost(eq(replacementURL), any())
        }
    }

    @Test
    fun `can override JsonRPC endpoint for ethr did resolver`() = runBlocking {

        val privateRpcUrl = "http://127.0.0.1:8545"
        val privateRegistry = "0x1234567890123456789012345678901234567890"

        val httpSpy = spyk(HttpClient())

        //eth_call to get owner
        coEvery { httpSpy.urlPost(any(), match { it.contains("0x8733d4e8") }) } returns
                """{"jsonrpc":"2.0","id":1,"result":"0x000000000000000000000000cf03dd0a894ef79cb5b601a43c4b25e3ae4c67ed"}"""
        //eth_call to get last changed
        coEvery { httpSpy.urlPost(any(), match { it.contains("0xf96d0f9f") }) } returns
                """{"jsonrpc":"2.0","id":1,"result":"0x0000000000000000000000000000000000000000000000000000000000000000"}"""
        //get logs
        coEvery { httpSpy.urlPost(any(), match { it.contains("eth_getLogs") }) } returns
                """{"jsonrpc":"2.0","id":1,"result":[]}"""

        val rpc = spyk(JsonRPC(privateRpcUrl, httpSpy))
        val resolver = EthrDIDResolver(rpc, privateRegistry)

        resolver.resolve("did:ethr:0xcf03dd0a894ef79cb5b601a43c4b25e3ae4c67ed")

        coVerify {
            httpSpy.urlPost(eq(privateRpcUrl), any())
            rpc.ethCall(eq(privateRegistry), any())
            rpc.getLogs(eq(privateRegistry), any(), any(), any())
        }
    }

    @Test
    fun `can override JsonRPC endpoint for JWT verification with ethr DID issuer`(): Unit = runBlocking {
        val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9.eyJjbGFpbSI6eyJuYW1lIjoiQm9iIiwiZ2VuZGVyIjoibWFsZSJ9LCJpYXQiOjE1NDk5MDg0MjQsImV4cCI6MTU0OTkwODcyNCwiaXNzIjoiZGlkOmV0aHI6MHhjZjAzZGQwYTg5NGVmNzljYjViNjAxYTQzYzRiMjVlM2FlNGM2N2VkIn0.ffjGFzoSfX-fS50GHhYkwA8It5034Rw8BczWslUcbfGI51uJSGbmhfJSfeGdEaPlFFgVrnRj1YBoG_oHrnEiBQA"

        val privateRpcUrl = "http://127.0.0.1:8545"
        val privateRegistry = "0x1234567890123456789012345678901234567890"

        val httpSpy = spyk(HttpClient())

        //eth_call to get owner
        coEvery { httpSpy.urlPost(any(), match { it.contains("0x8733d4e8") }) } returns
                """{"jsonrpc":"2.0","id":1,"result":"0x000000000000000000000000cf03dd0a894ef79cb5b601a43c4b25e3ae4c67ed"}"""
        //eth_call to get last changed
        coEvery { httpSpy.urlPost(any(), match { it.contains("0xf96d0f9f") }) } returns
                """{"jsonrpc":"2.0","id":1,"result":"0x0000000000000000000000000000000000000000000000000000000000000000"}"""
        //get logs
        coEvery { httpSpy.urlPost(any(), match { it.contains("eth_getLogs") }) } returns
                """{"jsonrpc":"2.0","id":1,"result":[]}"""

        val rpc = spyk(JsonRPC(privateRpcUrl, httpSpy))
        val resolver = EthrDIDResolver(rpc, privateRegistry)
        UniversalDID.registerResolver(resolver)

        JWTTools(TestTimeProvider(1549908724000))
                .verify(token)

        coVerify {
            httpSpy.urlPost(eq(privateRpcUrl), any())
        }

        UniversalDID.clearResolvers()
    }

}