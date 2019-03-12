package me.uport.sdk.universaldid

import assertk.assert
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlinx.coroutines.runBlocking
import me.uport.sdk.testhelpers.coAssert
import org.junit.Test

class UniversalDIDTest {

    private val testDDO = object : DIDDocument {
        override val context: String = "test context"
        override val id: String = "1234"
        override val publicKey: List<PublicKeyEntry> = emptyList()
        override val authentication: List<AuthenticationEntry> = emptyList()
        override val service: List<ServiceEntry> = emptyList()
    }

    private val testResolver = object : DIDResolver {
        override fun canResolve(potentialDID: String): Boolean = true

        override val method: String = "test"

        override suspend fun resolve(did: String): DIDDocument {
            return if (did.contains("test")) testDDO else throw IllegalArgumentException("can't use test resolver")
        }

    }

    @Test
    fun `blank resolves to error`() {
        UniversalDID.clearResolvers()

        coAssert {
            UniversalDID.resolve("")
        }.thrownError {
            isInstanceOf(IllegalArgumentException::class)
        }
    }

    @Test
    fun `testResolver resolves to error with blank`() {
        UniversalDID.clearResolvers()
        UniversalDID.registerResolver(testResolver)

        coAssert {
            UniversalDID.resolve("")
        }.thrownError {
            isInstanceOf(IllegalArgumentException::class)
        }
    }

    @Test
    fun `can register and find resolver`() = runBlocking {
        UniversalDID.clearResolvers()
        UniversalDID.registerResolver(testResolver)

        val ddo = UniversalDID.resolve("did:test:this is a test did")
        assert(ddo).isEqualTo(testDDO)
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
            val (method, _) = UniversalDID.parse(it)
            assert(method).isEqualTo("generic")
        }

        invalidDIDs.forEach {
            val (method, _) = UniversalDID.parse(it)
            assert(method).isEmpty()
        }
    }

}