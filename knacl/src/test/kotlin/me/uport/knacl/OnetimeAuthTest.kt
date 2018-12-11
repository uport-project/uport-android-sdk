@file:Suppress("PrivatePropertyName")

package me.uport.knacl

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class OnetimeAuthTest {

    class OnetimeAuthSpec(val m: ByteArray, val k: ByteArray, val out: ByteArray)

    private val crypto_onetimeauth_spec_vectors = listOf(
            OnetimeAuthSpec(
                    m = arrayOf(72, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100, 33).map { it.toByte() }.toByteArray(),
                    k = arrayOf(116, 104, 105, 115, 32, 105, 115, 32, 51, 50, 45, 98, 121, 116, 101, 32, 107, 101, 121, 32, 102, 111, 114, 32, 80, 111, 108, 121, 49, 51, 48, 53).map { it.toByte() }.toByteArray(),
                    out = arrayOf(166, 247, 69, 0, 143, 129, 201, 22, 162, 13, 204, 116, 238, 242, 178, 240).map { it.toByte() }.toByteArray()
            ),
            OnetimeAuthSpec(
                    m = ByteArray(32),
                    k = arrayOf(116, 104, 105, 115, 32, 105, 115, 32, 51, 50, 45, 98, 121, 116, 101, 32, 107, 101, 121, 32, 102, 111, 114, 32, 80, 111, 108, 121, 49, 51, 48, 53).map { it.toByte() }.toByteArray(),
                    out = arrayOf(73, 236, 120, 9, 14, 72, 30, 198, 194, 107, 51, 185, 28, 204, 3, 7).map { it.toByte() }.toByteArray()
            ),
            OnetimeAuthSpec(
                    m = ByteArray(2007),
                    k = arrayOf(116, 104, 105, 115, 32, 105, 115, 32, 51, 50, 45, 98, 121, 116, 101, 32, 107, 101, 121, 32, 102, 111, 114, 32, 80, 111, 108, 121, 49, 51, 48, 53).map { it.toByte() }.toByteArray(),
                    out = arrayOf(218, 132, 188, 171, 2, 103, 108, 56, 205, 176, 21, 96, 66, 116, 194, 170).map { it.toByte() }.toByteArray()
            ),
            OnetimeAuthSpec(
                    m = ByteArray(2007),
                    k = ByteArray(32),
                    out = ByteArray(16)
            ),
            OnetimeAuthSpec(
                    m = arrayOf(255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255).map { it.toByte() }.toByteArray(),
                    k = arrayOf(2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0).map { it.toByte() }.toByteArray(),
                    out = arrayOf(3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0).map { it.toByte() }.toByteArray()
            )
    )

    @Test
    fun `crypto_onetimeauth specified vectors`() {
        crypto_onetimeauth_spec_vectors.forEach { spec ->
            val out = ByteArray(16)
            NaClLowLevel.crypto_onetimeauth(out, 0, spec.m, 0, spec.m.size.toLong(), spec.k)
            assertArrayEquals(spec.out, out)
        }
    }
}