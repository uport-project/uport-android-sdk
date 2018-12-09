@file:Suppress("ObjectPropertyName", "FunctionName")

package me.uport.knacl

import java.security.SecureRandom

@ExperimentalUnsignedTypes
typealias u32 = UInt

@ExperimentalUnsignedTypes
typealias u64 = ULong

typealias i64 = Long

@Suppress("unused")
@ExperimentalUnsignedTypes
object NaClLowLevel {

    private val _0: UByteArray = UByteArray(16) { 0u }

    //XXX: check initialization, of this array
    val _9: UByteArray = UByteArray(32).apply { this[0] = 9u }

    private val gf0: ULongArray = ULongArray(16) { 0UL }
    private val gf1: ULongArray = ULongArray(16).apply { this[0] = 1UL }
    private val _121665: ULongArray = ulongArrayOf(0xDB41UL, 1UL, 0UL, 0UL, 0UL, 0UL, 0UL, 0UL, 0UL, 0UL, 0UL, 0UL, 0UL, 0UL, 0UL, 0UL)
    private val D: ULongArray = ulongArrayOf(0x78a3UL, 0x1359UL, 0x4dcaUL, 0x75ebUL, 0xd8abUL, 0x4141UL, 0x0a4dUL, 0x0070UL, 0xe898UL, 0x7779UL, 0x4079UL, 0x8cc7UL, 0xfe73UL, 0x2b6fUL, 0x6ceeUL, 0x5203UL)
    private val D2: ULongArray = ulongArrayOf(0xf159UL, 0x26b2UL, 0x9b94UL, 0xebd6UL, 0xb156UL, 0x8283UL, 0x149aUL, 0x00e0UL, 0xd130UL, 0xeef3UL, 0x80f2UL, 0x198eUL, 0xfce7UL, 0x56dfUL, 0xd9dcUL, 0x2406UL)
    private val X: ULongArray = ulongArrayOf(0xd51aUL, 0x8f25UL, 0x2d60UL, 0xc956UL, 0xa7b2UL, 0x9525UL, 0xc760UL, 0x692cUL, 0xdc5cUL, 0xfdd6UL, 0xe231UL, 0xc0a4UL, 0x53feUL, 0xcd6eUL, 0x36d3UL, 0x2169UL)
    private val Y: ULongArray = ulongArrayOf(0x6658UL, 0x6666UL, 0x6666UL, 0x6666UL, 0x6666UL, 0x6666UL, 0x6666UL, 0x6666UL, 0x6666UL, 0x6666UL, 0x6666UL, 0x6666UL, 0x6666UL, 0x6666UL, 0x6666UL, 0x6666UL)
    private val I: ULongArray = ulongArrayOf(0xa0b0UL, 0x4a0eUL, 0x1b27UL, 0xc4eeUL, 0xe478UL, 0xad2fUL, 0x1806UL, 0x2f43UL, 0xd7a7UL, 0x3dfbUL, 0x0099UL, 0x2b4dUL, 0xdf0bUL, 0x4fc1UL, 0x2480UL, 0x2b83UL)
    private fun L32(x: u32, c: Int): u32 = ((x shl c) or ((x and 0xffffffffu) shr (32 - c)))

    fun randombytes(size: Int): UByteArray {
        val arr = UByteArray(size)
        randombytes(arr, size)
        return arr
    }

    private fun randombytes(x: UByteArray, size: Int) {
        require(x.size >= size) { "array must be of size>=`$size` but it is of size=${x.size}" }
        SecureRandom().nextBytes(x.asByteArray())
    }

    private fun ld32(x: UByteArray, startFrom: Int = 0): u32 {
        require(x.size >= 4 + startFrom) { "array `x` of length ${x.size} must provide at least 4 elements starting from $startFrom for `UByte`s to `UInt` conversion" }
        var u: u32 = x[3 + startFrom].toUInt()
        u = (u shl 8) or x[2 + startFrom].toUInt()
        u = (u shl 8) or x[1 + startFrom].toUInt()
        u = (u shl 8) or x[0 + startFrom].toUInt()
        return u
    }

    private fun dl64(x: UByteArray, xi: Int): u64 {
        require(x.size >= 8 + xi) { "array must have at least 8 elements for `UByte`s to `ULong` conversion" }
        var u: u64 = 0UL
        for (i in 0 until 8) {
            u = (u shl 8) or x[i + xi].toULong()
        }
        return u
    }

    /**
     * converts UInt value [u] to array of UBytes and fills the resulting bytes in the output array [x] starting from [startFrom]
     */
    private fun st32(x: UByteArray, u: u32, startFrom: Int = 0) {
        require(x.size >= 4 + startFrom) { "`x` output array is too small to fit 4 bytes starting from $startFrom" }
        var uu = u
        for (i in 0 until 4) {
            x[i + startFrom] = (uu and 0xffu).toUByte()
            uu = uu shr 8
        }
    }

    /**
     * converts UInt to array of UBytes
     */
    fun st32(u: u32): UByteArray {
        val x = UByteArray(4)
        st32(x, u)
        return x
    }

    //XXX: converts ULong to array of UBytes in reverse order
    private fun ts64(x: UByteArray, xi: Int = 0, u: u64) {
        var uu = u
        for (i in 7 downTo 0) {
            x[i + xi] = (uu and 0xffu).toUByte()
            uu = uu shr 8
        }
    }

    private fun vn(x: UByteArray, xi: Int = 0, y: UByteArray, yi: Int, n: Int): Int {
        var d: u32 = 0u
        for (i in 0 until n) {
            d = d or (x[i + xi] xor y[i + yi]).toUInt()
        }
        return ((1u and ((d - 1u) shr 8)) - 1u).toInt()
    }

    private fun crypto_verify_16(x: UByteArray, xi: Int = 0, y: UByteArray, yi: Int = 0): Int {
        return vn(x, xi, y, yi, 16)
    }

    private fun crypto_verify_32(x: UByteArray, xi: Int = 0, y: UByteArray, yi: Int = 0): Int {
        return vn(x, xi, y, yi, 32)
    }

    private fun core(outArr: UByteArray, inArr: UByteArray, k: UByteArray, c: UByteArray, h: Int) {
        val w = UIntArray(16)
        val x = UIntArray(16)
        val y = UIntArray(16)
        val t = UIntArray(4)

        for (i in 0 until 4) {
            x[5 * i] = ld32(c, 4 * i)
            x[1 + i] = ld32(k, 4 * i)
            x[6 + i] = ld32(inArr, 4 * i)
            x[11 + i] = ld32(k, 16 + 4 * i)
        }

        for (i in 0 until 16) {
            y[i] = x[i]
        }

        for (i in 0 until 20) {
            for (j in 0 until 4) {
                for (m in 0 until 4) {
                    t[m] = x[(5 * j + 4 * m) % 16]
                }
                t[1] = t[1] xor L32(t[0] + t[3], 7)
                t[2] = t[2] xor L32(t[1] + t[0], 9)
                t[3] = t[3] xor L32(t[2] + t[1], 13)
                t[0] = t[0] xor L32(t[3] + t[2], 18)
                for (m in 0 until 4) {
                    w[4 * j + (j + m) % 4] = t[m]
                }
            }
            for (m in 0 until 16) {
                x[m] = w[m]
            }
        }

        if (h != 0) {
            for (i in 0 until 16) {
                x[i] += y[i]
            }
            for (i in 0 until 4) {
                x[5 * i] -= ld32(c, 4 * i)
                x[6 + i] -= ld32(inArr, 4 * i)
            }
            for (i in 0 until 4) {
                st32(outArr, x[5 * i], 4 * i)
                st32(outArr, x[6 + i], 16 + 4 * i)
            }
        } else {
            for (i in 0 until 16) {
                st32(outArr, x[i] + y[i], 4 * i)
            }
        }
    }

    fun crypto_core_salsa20(outArr: UByteArray, inArr: UByteArray, k: UByteArray, c: UByteArray): Int {
        core(outArr, inArr, k, c, 0)
        return 0
    }

    fun crypto_core_hsalsa20(outArr: UByteArray, inArr: UByteArray, k: UByteArray, c: UByteArray): Int {
        core(outArr, inArr, k, c, 1)
        return 0
    }

    private val sigma: UByteArray = "expand 32-byte k".toByteArray(Charsets.UTF_8).asUByteArray()

    fun crypto_stream_salsa20_xor(c: UByteArray, m: UByteArray?, bIn: u64, n: UByteArray, k: UByteArray, nStart: Int = 0): Int {
        val z = UByteArray(16) { 0u }
        val x = UByteArray(64)
        var u: u32
        if (bIn == 0uL) return 0
        for (i in 0 until 8) {
            z[i] = n[i + nStart]
        }
        var b = bIn
        var cCounter = 0
        var mCounter = 0
        while (b >= 64u) {
            crypto_core_salsa20(x, z, k, sigma)
            for (i in 0 until 64) {
                c[i + cCounter] = (if (m != null) m[i + mCounter] else 0u) xor x[i]
            }
            u = 1u
            for (i in 8 until 16) {
                u += z[i]
                z[i] = (u and 0xffu).toUByte()
                u = u shr 8
            }
            b -= 64u
            cCounter += 64
            if (m != null) mCounter += 64
        }
        if (b != 0uL) {
            crypto_core_salsa20(x, z, k, sigma)
            for (i in 0 until b.toInt()) {
                c[i + cCounter] = (if (m != null) m[i + mCounter] else 0u) xor x[i]
            }
        }
        return 0
    }

    fun crypto_stream_salsa20(c: UByteArray, d: u64, n: UByteArray, k: UByteArray, nStart: Int = 0): Int {
        return crypto_stream_salsa20_xor(c, null, d, n, k, nStart)
    }

    fun crypto_stream(c: UByteArray, d: u64, n: UByteArray, k: UByteArray): Int {
        val s = UByteArray(32)
        crypto_core_hsalsa20(s, n, k, sigma)
        return crypto_stream_salsa20(c, d, n, s, 16)
    }

    fun crypto_stream_xor(c: UByteArray, m: UByteArray, d: u64, n: UByteArray, k: UByteArray): Int {
        val s = UByteArray(32)
        crypto_core_hsalsa20(s, n, k, sigma)
        return crypto_stream_salsa20_xor(c, m, d, n, s, 16)
    }

    private fun add1305(h: UIntArray, c: UIntArray) {
        var u: u32 = 0u
        for (j in 0 until 17) {
            u += h[j] + c[j]
            h[j] = u and 255u
            u = u shr 8
        }
    }

    private val minusp: UIntArray = intArrayOf(5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 252).asUIntArray()

    fun crypto_onetimeauth(out: UByteArray, outStart: Int, m: UByteArray, mStart: Int, n: u64, k: UByteArray): Int {
        var mpos = mStart
        var nn = n
        val x = UIntArray(17)
        val r = UIntArray(17)
        val h = UIntArray(17)
        val c = UIntArray(17)
        val g = UIntArray(17)

        for (j in 0 until 17) {
            r[j] = 0u; h[j] = 0u
        }
        for (j in 0 until 16) {
            r[j] = k[j].toUInt()
        }
        r[3] = r[3] and 15u
        r[4] = r[4] and 252u
        r[7] = r[7] and 15u
        r[8] = r[8] and 252u
        r[11] = r[11] and 15u
        r[12] = r[12] and 252u
        r[15] = r[15] and 15u

        while (nn > 0u) {
            for (j in 0 until 17) {
                c[j] = 0u
            }
            var jj = 0
            while (jj < 16 && jj < nn.toInt()) {
                c[jj] = m[mpos + jj].toUInt()
                jj++
            }
            c[jj] = 1u
            mpos += jj; nn -= jj.toUInt()
            add1305(h, c)
            for (i in 0 until 17) {
                x[i] = 0u
                for (j in 0 until 17) {
                    x[i] += h[j] * if (j <= i) r[i - j] else (320u * r[i + 17 - j])
                }
            }
            for (i in 0 until 17) {
                h[i] = x[i]
            }
            var u = 0u
            for (j in 0 until 16) {
                u += h[j]
                h[j] = u and 255u
                u = u shr 8
            }
            u += h[16]
            h[16] = u and 3u
            u = (5u * (u shr 2))
            for (j in 0 until 16) {
                u += h[j]
                h[j] = u and 255u
                u = u shr 8
            }
            u += h[16]
            h[16] = u
        }

        for (j in 0 until 17) {
            g[j] = h[j]
        }
        add1305(h, minusp)
        val s = (0 - (h[16] shr 7).toInt()).toUInt()
        for (j in 0 until 17) {
            h[j] = h[j] xor (s and (g[j] xor h[j]))
        }

        for (j in 0 until 16) {
            c[j] = k[j + 16].toUInt()
        }
        c[16] = 0u
        add1305(h, c)
        for (j in 0 until 16) out[outStart + j] = h[j].toUByte()
        return 0
    }

    private fun crypto_onetimeauth_verify(h: UByteArray, hi: Int, m: UByteArray, mi: Int, n: u64, k: UByteArray): Int {
        val x = UByteArray(16)
        crypto_onetimeauth(x, 0, m, mi, n, k)
        return crypto_verify_16(h, hi, x, 0)
    }

    fun crypto_secretbox(c: UByteArray, m: UByteArray, d: u64, n: UByteArray, k: UByteArray): Int {
        if (d < 32u) return -1
        crypto_stream_xor(c, m, d, n, k)
        crypto_onetimeauth(c, 16, c, 32, d - 32u, c)
        for (i in 0 until 16) c[i] = 0u
        return 0
    }

    fun crypto_secretbox_open(m: UByteArray, c: UByteArray, d: u64, n: UByteArray, k: UByteArray): Int {
        val x = UByteArray(32)
        if (d < 32u) return -1
        crypto_stream(x, 32u, n, k)
        if (crypto_onetimeauth_verify(c, 16, c, 32, d - 32u, x) != 0) return -1
        crypto_stream_xor(m, c, d, n, k)
        for (i in 0 until 32) m[i] = 0u
        return 0
    }

///////////////////////////////////////////////////////////////////
// curve 25519
///////////////////////////////////////////////////////////////////

    private fun set25519(r: ULongArray, a: ULongArray) {
        for (i in 0 until 16) r[i] = a[i]
    }

    private fun car25519(o: ULongArray) {
        var c: i64
        for (i in 0 until 16) {
            o[i] += (1uL shl 16)
            c = (o[i] shr 16).toLong()
            val j = if (i < 15) 1 else 0
            val k = if (i == 15) 1 else 0
            o[(i + 1) * j] += (c - 1 + 37 * (c - 1) * k).toULong()
            o[i] -= (c shl 16).toULong()
        }
    }

    private fun sel25519(p: ULongArray, q: ULongArray, b: Int) {
        var t: u64
        val c = ((b - 1).inv()).toULong()
        for (i in 0 until 16) {
            t = c and (p[i] xor q[i])
            p[i] = p[i] xor t
            q[i] = q[i] xor t
        }
    }

    private fun pack25519(o: UByteArray, n: ULongArray) {
        var b: Int
        val m = ULongArray(16)
        val t = ULongArray(16)
        for (i in 0 until 16) t[i] = n[i]
        car25519(t)
        car25519(t)
        car25519(t)
        for (j in 0 until 2) {
            m[0] = t[0] - 0xffedu
            for (i in 1 until 15) {
                m[i] = t[i] - 0xffffu - ((m[i - 1] shr 16) and 1u)
                m[i - 1] = m[i - 1] and 0xffffu
            }
            m[15] = t[15] - 0x7fffu - ((m[14] shr 16) and 1u)
            b = ((m[15] shr 16) and 1u).toInt()
            m[14] = m[14] and 0xffffu
            sel25519(t, m, 1 - b)
        }
        for (i in 0 until 16) {
            o[2 * i] = (t[i] and 0xffu).toUByte()
            o[2 * i + 1] = ((t[i] shr 8) and 0xffu).toUByte()
        }
    }

    private fun neq25519(a: ULongArray, b: ULongArray): Int {
        val c = UByteArray(32)
        val d = UByteArray(32)
        pack25519(c, a)
        pack25519(d, b)
        return crypto_verify_32(c, 0, d, 0)
    }

    private fun par25519(a: ULongArray): UByte {
        val d = UByteArray(32)
        pack25519(d, a)
        return (d[0] and 1u)
    }

    private fun unpack25519(o: ULongArray, n: UByteArray) {
        for (i in 0 until 16) {
            o[i] = n[2 * i] + ((n[2 * i + 1]).toLong() shl 8).toULong()
        }
        o[15] = o[15] and 0x7fffu
    }

    private fun A(o: ULongArray, a: ULongArray, b: ULongArray) {
        for (i in 0 until 16) o[i] = a[i] + b[i]
    }

    private fun Z(o: ULongArray, a: ULongArray, b: ULongArray) {
        for (i in 0 until 16) o[i] = a[i] - b[i]
    }

    private fun M(o: ULongArray, a: ULongArray, b: ULongArray) {
        val t = ULongArray(31)

        for (i in 0 until 16) {
            for (j in 0 until 16) {
                t[i + j] += a[i] * b[j]
            }
        }
        for (i in 0 until 15) {
            t[i] += 38u * t[i + 16]
        }
        for (i in 0 until 16) {
            o[i] = t[i]
        }
        car25519(o)
        car25519(o)
    }

    private fun S(o: ULongArray, a: ULongArray) = M(o, a, a)

    private fun inv25519(o: ULongArray, i: ULongArray) {
        val c = ULongArray(16)
        for (a in 0 until 16) c[a] = i[a]
        for (a in 253 downTo 0) {
            S(c, c)
            if (a != 2 && a != 4) M(c, c, i)
        }
        for (a in 0 until 16) o[a] = c[a]
    }

    private fun pow2523(o: ULongArray, i: ULongArray) {
        val c = ULongArray(16)
        for (a in 0 until 16) c[a] = i[a]
        for (a in 250 downTo 0) {
            S(c, c)
            if (a != 1) M(c, c, i)
        }
        for (a in 0 until 16) o[a] = c[a]
    }

    fun crypto_scalarmult(q: UByteArray, n: UByteArray, p: UByteArray): Int {
        val z = UByteArray(32)
        val x = LongArray(80)
        var r: Int
        val a = ULongArray(16)
        val b = ULongArray(16)
        val c = ULongArray(16)
        val d = ULongArray(16)
        val e = ULongArray(16)
        val f = ULongArray(16)
        for (i in 0 until 31) {
            z[i] = n[i]
        }
        z[31] = (n[31] and 127u) or 64u
        z[0] = z[0] and 248u
        unpack25519(x.asULongArray(), p)
        for (i in 0 until 16) {
            b[i] = x[i].toULong()
            d[i] = 0u
            a[i] = 0u
            c[i] = 0u
        }
        a[0] = 1u
        d[0] = 1u
        for (i in 254 downTo 0) {
            r = ((z[i shr 3].toULong() shr (i and 7)) and 1u).toInt()
            sel25519(a, b, r)
//            println(a.joinToString())
            sel25519(c, d, r)
            A(e, a, c)
            Z(a, a, c)
            A(c, b, d)
            Z(b, b, d)
            S(d, e)
            S(f, a)
            M(a, c, a)
            M(c, b, e)
            A(e, a, c)
            Z(a, a, c)
            S(b, a)
            Z(c, d, f)
            M(a, c, _121665)
            A(a, a, d)
            M(c, c, a)
            M(a, d, f)
            M(d, b, x.asULongArray())
            S(b, e)
            sel25519(a, b, r)
            sel25519(c, d, r)
        }
        for (i in 0 until 16) {
            x[i + 16] = a[i].toLong()
            x[i + 32] = c[i].toLong()
            x[i + 48] = b[i].toLong()
            x[i + 64] = d[i].toLong()
        }

        val x32 = x.copyOfRange(32, x.size).asULongArray()
        val x16 = x.copyOfRange(16, x.size).asULongArray()
        inv25519(x32, x32)
        M(x16, x16, x32)
        pack25519(q, x16)

        return 0
    }

    fun crypto_scalarmult_base(q: UByteArray, n: UByteArray): Int {
        return crypto_scalarmult(q, n, _9)
    }

    fun crypto_box_keypair(y: UByteArray, x: UByteArray): Int {
        randombytes(x, 32)
        return crypto_scalarmult_base(y, x)
    }

    fun crypto_box_beforenm(k: UByteArray, y: UByteArray, x: UByteArray): Int {
        val s = UByteArray(32)
        crypto_scalarmult(s, x, y)
        return crypto_core_hsalsa20(k, _0, s, sigma)
    }

    private fun crypto_box_afternm(c: UByteArray, m: UByteArray, d: u64, n: UByteArray, k: UByteArray): Int {
        return crypto_secretbox(c, m, d, n, k)
    }

    private fun crypto_box_open_afternm(m: UByteArray, c: UByteArray, d: u64, n: UByteArray, k: UByteArray): Int {
        return crypto_secretbox_open(m, c, d, n, k)
    }

    fun crypto_box(c: UByteArray, m: UByteArray, d: u64, n: UByteArray, y: UByteArray, x: UByteArray): Int {
        val k = UByteArray(32)
        crypto_box_beforenm(k, y, x)
        return crypto_box_afternm(c, m, d, n, k)
    }

    fun crypto_box_open(m: UByteArray, c: UByteArray, d: u64, n: UByteArray, y: UByteArray, x: UByteArray): Int {
        val k = UByteArray(32)
        crypto_box_beforenm(k, y, x)
        return crypto_box_open_afternm(m, c, d, n, k)
    }

    /////////////////////////////////////////////////////
    // hash
    /////////////////////////////////////////////////////


    fun R(x: u64, c: Int): u64 = ((x shr c) or (x shl (64 - c)))

    private fun Ch(x: u64, y: u64, z: u64): u64 = (x and y) xor (x.inv() and z)

    private fun Maj(x: u64, y: u64, z: u64): u64 = ((x and y) xor (x xor z) xor (y and z))

    private fun Sigma0(x: u64): u64 = (R(x, 28) xor R(x, 34) xor R(x, 39))

    private fun Sigma1(x: u64): u64 = (R(x, 14) xor R(x, 18) xor R(x, 41))

    private fun sigma0(x: u64): u64 = (R(x, 1) xor R(x, 8) xor (x shr 7))

    private fun sigma1(x: u64): u64 = (R(x, 19) xor R(x, 61) xor (x shr 6))

    private val K = ulongArrayOf(
            0x428a2f98d728ae22UL, 0x7137449123ef65cdUL, 0xb5c0fbcfec4d3b2fUL, 0xe9b5dba58189dbbcUL,
            0x3956c25bf348b538UL, 0x59f111f1b605d019UL, 0x923f82a4af194f9bUL, 0xab1c5ed5da6d8118UL,
            0xd807aa98a3030242UL, 0x12835b0145706fbeUL, 0x243185be4ee4b28cUL, 0x550c7dc3d5ffb4e2UL,
            0x72be5d74f27b896fUL, 0x80deb1fe3b1696b1UL, 0x9bdc06a725c71235UL, 0xc19bf174cf692694UL,
            0xe49b69c19ef14ad2UL, 0xefbe4786384f25e3UL, 0x0fc19dc68b8cd5b5UL, 0x240ca1cc77ac9c65UL,
            0x2de92c6f592b0275UL, 0x4a7484aa6ea6e483UL, 0x5cb0a9dcbd41fbd4UL, 0x76f988da831153b5UL,
            0x983e5152ee66dfabUL, 0xa831c66d2db43210UL, 0xb00327c898fb213fUL, 0xbf597fc7beef0ee4UL,
            0xc6e00bf33da88fc2UL, 0xd5a79147930aa725UL, 0x06ca6351e003826fUL, 0x142929670a0e6e70UL,
            0x27b70a8546d22ffcUL, 0x2e1b21385c26c926UL, 0x4d2c6dfc5ac42aedUL, 0x53380d139d95b3dfUL,
            0x650a73548baf63deUL, 0x766a0abb3c77b2a8UL, 0x81c2c92e47edaee6UL, 0x92722c851482353bUL,
            0xa2bfe8a14cf10364UL, 0xa81a664bbc423001UL, 0xc24b8b70d0f89791UL, 0xc76c51a30654be30UL,
            0xd192e819d6ef5218UL, 0xd69906245565a910UL, 0xf40e35855771202aUL, 0x106aa07032bbd1b8UL,
            0x19a4c116b8d2d0c8UL, 0x1e376c085141ab53UL, 0x2748774cdf8eeb99UL, 0x34b0bcb5e19b48a8UL,
            0x391c0cb3c5c95a63UL, 0x4ed8aa4ae3418acbUL, 0x5b9cca4f7763e373UL, 0x682e6ff3d6b2b8a3UL,
            0x748f82ee5defb2fcUL, 0x78a5636f43172f60UL, 0x84c87814a1f0ab72UL, 0x8cc702081a6439ecUL,
            0x90befffa23631e28UL, 0xa4506cebde82bde9UL, 0xbef9a3f7b2c67915UL, 0xc67178f2e372532bUL,
            0xca273eceea26619cUL, 0xd186b8c721c0c207UL, 0xeada7dd6cde0eb1eUL, 0xf57d4f7fee6ed178UL,
            0x06f067aa72176fbaUL, 0x0a637dc5a2c898a6UL, 0x113f9804bef90daeUL, 0x1b710b35131c471bUL,
            0x28db77f523047d84UL, 0x32caab7b40c72493UL, 0x3c9ebe0a15c9bebcUL, 0x431d67c49c100d4cUL,
            0x4cc5d4becb3e42b6UL, 0x597f299cfc657e2aUL, 0x5fcb6fab3ad6faecUL, 0x6c44198c4a475817UL
    )

    private fun crypto_hashblocks(x: UByteArray, m: UByteArray, n: u64): Int {
        val z = ULongArray(8)
        val b = ULongArray(8)
        val a = ULongArray(8)
        val w = ULongArray(16)

        var t: u64

        for (i in 0 until 8) {
            z[i] = dl64(x, 8 * i)
            a[i] = dl64(x, 8 * i)
        }
        var nn = n
        var mi = 0

        while (nn >= 128u) {
            for (i in 0 until 16) {
                w[i] = dl64(m, mi + 8 * i)
            }

            for (i in 0 until 80) {
                for (j in 0 until 8) {
                    b[j] = a[j]
                }
                t = a[7] + Sigma1(a[4]) + Ch(a[4], a[5], a[6]) + K[i] + w[i % 16]
                b[7] = t + Sigma0(a[0]) + Maj(a[0], a[1], a[2])
                b[3] += t
                for (j in 0 until 8) {
                    a[(j + 1) % 8] = b[j]
                }
                if (i % 16 == 15) {
                    for (j in 0 until 16) {
                        w[j] += w[(j + 9) % 16] + sigma0(w[(j + 1) % 16]) + sigma1(w[(j + 14) % 16])
                    }
                }
            }


            for (i in 0 until 8) {
                a[i] += z[i]; z[i] = a[i]; }

            mi += 128
            nn -= 128u
        }

        for (i in 0 until 8) ts64(x, 8 * i, z[i])

        return n.toInt()
    }

    private val iv = ubyteArrayOf(
            0x6au, 0x09u, 0xe6u, 0x67u, 0xf3u, 0xbcu, 0xc9u, 0x08u,
            0xbbu, 0x67u, 0xaeu, 0x85u, 0x84u, 0xcau, 0xa7u, 0x3bu,
            0x3cu, 0x6eu, 0xf3u, 0x72u, 0xfeu, 0x94u, 0xf8u, 0x2bu,
            0xa5u, 0x4fu, 0xf5u, 0x3au, 0x5fu, 0x1du, 0x36u, 0xf1u,
            0x51u, 0x0eu, 0x52u, 0x7fu, 0xadu, 0xe6u, 0x82u, 0xd1u,
            0x9bu, 0x05u, 0x68u, 0x8cu, 0x2bu, 0x3eu, 0x6cu, 0x1fu,
            0x1fu, 0x83u, 0xd9u, 0xabu, 0xfbu, 0x41u, 0xbdu, 0x6bu,
            0x5bu, 0xe0u, 0xcdu, 0x19u, 0x13u, 0x7eu, 0x21u, 0x79u
    )

    private fun crypto_hash(outArr: UByteArray, m: UByteArray, n: u64): Int {
        require(outArr.size >= 64) { "outArr size(${outArr.size}) needs to be at least 64" }
        val h = iv.copyOf()
        val x = UByteArray(256)
        val b: u64 = n

        crypto_hashblocks(h, m, n)

        var mi = 0
        var nn = n.toInt()

        mi += nn
        nn = nn and 127
        mi -= nn

        for (i in 0 until nn) {
            x[i] = m[i + mi]
        }
        x[nn] = 128u

        nn = 256 - 128 * (if (nn < 112) 1 else 0)
        x[nn - 9] = ((b shr 61) and 0xffu).toUByte()
        ts64(x, nn - 8, b shl 3)
        crypto_hashblocks(h, x, nn.toULong())

        for (i in 0 until 64) outArr[i] = h[i]

        return 0
    }

    fun add(p: Array<ULongArray>, q: Array<ULongArray>) {
        val a = ULongArray(16)
        val b = ULongArray(16)
        val c = ULongArray(16)
        val d = ULongArray(16)
        val e = ULongArray(16)
        val f = ULongArray(16)
        val g = ULongArray(16)
        val h = ULongArray(16)
        val t = ULongArray(16)

        Z(a, p[1], p[0])
        Z(t, q[1], q[0])
        M(a, a, t)
        A(b, p[0], p[1])
        A(t, q[0], q[1])
        M(b, b, t)
        M(c, p[3], q[3])
        M(c, c, D2)
        M(d, p[2], q[2])
        A(d, d, d)
        Z(e, b, a)
        Z(f, d, c)
        A(g, d, c)
        A(h, b, a)

        M(p[0], e, f)
        M(p[1], h, g)
        M(p[2], g, f)
        M(p[3], e, h)
    }

    private fun cswap(p: Array<ULongArray>, q: Array<ULongArray>, b: UByte) {
        for (i in 0 until 4) {
            sel25519(p[i], q[i], b.toInt())
        }
    }

    private fun pack(r: UByteArray, p: Array<ULongArray>) {
        val tx = ULongArray(16)
        val ty = ULongArray(16)
        val zi = ULongArray(16)
        inv25519(zi, p[2])
        M(tx, p[0], zi)
        M(ty, p[1], zi)
        pack25519(r, ty)
        r[31] = r[31] xor ((par25519(tx).toUInt() shl 7) and 0xffu).toUByte()
    }

    private fun scalarmult(p: Array<ULongArray>, q: Array<ULongArray>, s: UByteArray) {
        set25519(p[0], gf0)
        set25519(p[1], gf1)
        set25519(p[2], gf1)
        set25519(p[3], gf0)
        for (i in 255 downTo 0) {
            val b: UByte = ((s[i / 8].toUInt() shr (i and 7)) and 1u).toUByte()
            cswap(p, q, b)
            add(q, p)
            add(p, p)
            cswap(p, q, b)
        }
    }

    private fun scalarbase(p: Array<ULongArray>, s: UByteArray) {
        val q = Array(4) { ULongArray(16) }
        set25519(q[0], X)
        set25519(q[1], Y)
        set25519(q[2], gf1)
        M(q[3], X, Y)
        scalarmult(p, q, s)
    }

    //XXX: check array sizes (32, 64)?
    fun crypto_sign_keypair(pk: UByteArray, sk: UByteArray): Int {
        val d = UByteArray(64)
        val p = Array(4) { ULongArray(16) }

        randombytes(sk, 32)
        crypto_hash(d, sk, 32u)
        d[0] = d[0] and 248u
        d[31] = d[31] and 127u
        d[31] = d[31] or 64u

        scalarbase(p, d)
        pack(pk, p)

        for (i in 0 until 32) sk[32 + i] = pk[i]
        return 0
    }

    private val L = longArrayOf(
            0xed, 0xd3, 0xf5, 0x5c,
            0x1a, 0x63, 0x12, 0x58,
            0xd6, 0x9c, 0xf7, 0xa2,
            0xde, 0xf9, 0xde, 0x14,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0x10)

    private fun modL(r: UByteArray, x: LongArray, ri: Int = 0) {
        var carry: i64
        for (i in 63 downTo 32) {
            carry = 0
            for (j in (i - 32) until (i - 12)) {
                x[j] += carry - 16 * x[i] * L[j - (i - 32)]
                carry = (x[j] + 128) shr 8
                x[j] -= carry shl 8
            }
            ///XXX: check index arithmetic
            x[i - 12 - 1] += carry
            x[i] = 0
        }
        carry = 0
        for (j in 0 until 32) {
            x[j] += carry - (x[31] shr 4) * L[j]
            carry = x[j] shr 8
            x[j] = x[j] and 255
        }
        for (j in 0 until 32) {
            x[j] -= carry * L[j]
        }
        for (i in 0 until 32) {
            x[i + 1] += x[i] shr 8
            r[i + ri] = (x[i] and 255).toUByte()
        }
    }

    private fun reduce(r: UByteArray) {
        val x = LongArray(64)
        for (i in 0 until 64) {
            x[i] = r[i].toLong()
            r[i] = 0u
            //xxx: check result
        }
        modL(r, x)
    }

    fun crypto_sign(sm: UByteArray, m: UByteArray, n: u64, sk: UByteArray): Int {
        require(sm.size >= n.toLong() + 64) { "resulting array sm(size=${sm.size}) must be able to fit n+64 bytes (${n + 64u})" }
        val d = UByteArray(64)
        val h = UByteArray(64)
        val r = UByteArray(64)
        val x = LongArray(64)
        val p = Array(4) { ULongArray(16) }

        crypto_hash(d, sk, 32u)
        d[0] = d[0] and 248u
        d[31] = d[31] and 127u
        d[31] = d[31] or 64u

        for (i in 0 until n.toInt()) {
            sm[64 + i] = m[i]
        }
        for (i in 0 until 32) {
            sm[32 + i] = d[32 + i]
        }

        crypto_hash(r, sm.copyOfRange(32, sm.size), n + 32u)
        reduce(r)
        scalarbase(p, r)
        pack(sm, p)

        for (i in 0 until 32) {
            sm[i + 32] = sk[i + 32]
        }
        crypto_hash(h, sm, n + 64u)
        reduce(h)

        for (i in 0 until 64) {
            x[i] = 0
        }
        for (i in 0 until 32) {
            x[i] = r[i].toLong()
        }
        for (i in 0 until 32) for (j in 0 until 32) {
            x[i + j] += (h[i] * d[j]).toLong()
        }
        modL(sm, x, 32)

        return 0
    }

    //check lengths r[4], p[32]
    private fun unpackneg(r: Array<ULongArray>, p: UByteArray): Int {
        val t = ULongArray(16)
        val chk = ULongArray(16)
        val num = ULongArray(16)
        val den = ULongArray(16)
        val den2 = ULongArray(16)
        val den4 = ULongArray(16)
        val den6 = ULongArray(16)
        set25519(r[2], gf1)
        unpack25519(r[1], p)
        S(num, r[1])
        M(den, num, D)
        Z(num, num, r[2])
        A(den, r[2], den)

        S(den2, den)
        S(den4, den2)
        M(den6, den4, den2)
        M(t, den6, num)
        M(t, t, den)

        pow2523(t, t)
        M(t, t, num)
        M(t, t, den)
        M(t, t, den)
        M(r[0], t, den)

        S(chk, r[0])
        M(chk, chk, den)
        if (neq25519(chk, num) != 0) {
            M(r[0], r[0], I)
        }

        S(chk, r[0])
        M(chk, chk, den)
        if (neq25519(chk, num) != 0) {
            return -1
        }

        if (par25519(r[0]) == ((p[31].toUInt() shr 7) and 0xffu).toUByte()) {
            Z(r[0], gf0, r[0])
        }

        M(r[3], r[0], r[1])
        return 0
    }

    fun crypto_sign_open(m: UByteArray, sm: UByteArray, n: u64, pk: UByteArray): Int {
        val nn = (n - 64u).toInt()
        require(m.size >= nn) { "resulting array `m` size must be at least $nn but is ${m.size}" }
        val t = UByteArray(32)
        val h = UByteArray(64)
        val p = Array(4) { ULongArray(16) }
        val q = Array(4) { ULongArray(16) }

        if (n < 64u) {
            return -1
        }

        if (unpackneg(q, pk) != 0) {
            return -1
        }

        for (i in 0 until n.toInt()) {
            m[i] = sm[i]
        }
        for (i in 0 until 32) m[i + 32] = pk[i]
        crypto_hash(h, m, n)
        reduce(h)
        scalarmult(p, q, h)

        scalarbase(q, sm.copyOfRange(32, sm.size))
        add(p, q)
        pack(t, p)

        if (crypto_verify_32(sm, 0, t, 0) != 0) {
            for (i in 0 until nn) {
                m[i] = 0u
            }
            return -1
        }

        for (i in 0 until nn) m[i] = sm[i + 64]
        return 0
    }
}
