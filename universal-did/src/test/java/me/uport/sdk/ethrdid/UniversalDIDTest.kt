package me.uport.sdk.ethrdid

import kotlinx.coroutines.experimental.runBlocking
import me.uport.sdk.universaldid.DIDDocument
import me.uport.sdk.universaldid.DIDResolver
import me.uport.sdk.universaldid.UniversalDID
import org.junit.Assert.*
import org.junit.Test
import java.lang.IllegalArgumentException

class UniversalDIDTest {

    private val testDDO = object : DIDDocument {
        val unusedField = "test document"
    }

    private val testResolver = object : DIDResolver {
        override fun canResolve(potentialDID: String): Boolean = true

        override val method: String = "test"

        override suspend fun resolve(did: String): DIDDocument {
            return if (did.contains("test")) testDDO else throw IllegalArgumentException("can't use test resolver")
        }

    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank resolves to error`() = runBlocking {
        UniversalDID.clearResolvers()

        val unusedDdo = UniversalDID.resolve("")
    }

    @Test(expected = Exception::class)
    fun `testResolver resolves to error with blank`() = runBlocking {
        UniversalDID.clearResolvers()
        UniversalDID.registerResolver(testResolver)

        val unusedDdo = UniversalDID.resolve("")
    }

    @Test
    fun `can register and find resolver`() = runBlocking {
        UniversalDID.clearResolvers()
        UniversalDID.registerResolver(testResolver)

        val ddo = UniversalDID.resolve("did:test:this is a test did")
        assertEquals(testDDO, ddo)
    }

    private val validDIDs = listOf(
            "did:generic:0x0011223344556677889900112233445566778899",
            "did:generic:01234",
            "did:generic:has spaces",
            "did:generic:more:colons",
            "did:generic:01234#fragment-attached",
            "did:generic:01234?key=value",
            "did:generic:01234?key=value&other-key=other-value"
    )

    private val invalidDIDs = listOf(
            "",
            "0x0011223344556677889900112233445566778899",
            "ethr:0x0011223344556677889900112233445566778899",
            "did:ethr",
            "did::something",
            "did:ethr:"
    )

    @Test
    fun `parses dids correctly`() {
        validDIDs.forEach {
            val (method, identifier) = UniversalDID.parse(it)
            assertTrue("parsing $it failed, got (method=$method, identifier=$identifier)", method.isNotBlank())
            assertEquals("generic", method)
        }

        invalidDIDs.forEach {
            val (method, identifier) = UniversalDID.parse(it)
            assertTrue("parsing $it should have failed, got (method=$method, identifier=$identifier)", method.isBlank())
        }
    }

}