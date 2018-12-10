package me.uport.knacl

import me.uport.knacl.NaClLowLevel.crypto_scalarmult_base

/**
 * This class exposes the methods from the NaCl library that are used by this SDK
 */
object nacl {

    val crypto_secretbox_KEYBYTES = 32
    val crypto_secretbox_NONCEBYTES = 24
    val crypto_secretbox_ZEROBYTES = 32
    val crypto_secretbox_BOXZEROBYTES = 16
    val crypto_scalarmult_BYTES = 32
    val crypto_scalarmult_SCALARBYTES = 32
    val crypto_box_PUBLICKEYBYTES = 32
    val crypto_box_SECRETKEYBYTES = 32
    val crypto_box_BEFORENMBYTES = 32
    val crypto_box_NONCEBYTES = crypto_secretbox_NONCEBYTES
    val crypto_box_ZEROBYTES = crypto_secretbox_ZEROBYTES
    val crypto_box_BOXZEROBYTES = crypto_secretbox_BOXZEROBYTES
    private val crypto_sign_BYTES = 64
    private val crypto_sign_PUBLICKEYBYTES = 32
    private val crypto_sign_SECRETKEYBYTES = 64
    private val crypto_sign_SEEDBYTES = 32
    private val crypto_hash_BYTES = 64

    private fun checkLengths(k: ByteArray, n: ByteArray) {
        require(k.size == crypto_secretbox_KEYBYTES) { "bad key size" }
        require(n.size == crypto_secretbox_NONCEBYTES) { "bad nonce size" }
    }

    private fun checkBoxLengths(pk: ByteArray, sk: ByteArray) {
        require(pk.size == crypto_box_PUBLICKEYBYTES) { "bad public key size" }
        require(sk.size == crypto_box_SECRETKEYBYTES) { "bad secret key size" }
    }

    //FIXME: this should be outsourced to a higher level API
    fun randomBytes(size: Int) = NaClLowLevel.randombytes(size)

    fun secretbox(msg: ByteArray, nonce: ByteArray, key: ByteArray): ByteArray {
        checkLengths(key, nonce)
        val m = ByteArray(crypto_secretbox_ZEROBYTES + msg.size)
        val c = ByteArray(m.size)
        msg.copyInto(m, crypto_secretbox_ZEROBYTES)
        NaClLowLevel.crypto_secretbox(c, m, m.size.toLong(), nonce, key)
        return c.copyOfRange(crypto_secretbox_BOXZEROBYTES, c.size)
    }

    fun secretboxOpen(box: ByteArray, nonce: ByteArray, key: ByteArray): ByteArray? {
        checkLengths(key, nonce)
        val ciphertext = ByteArray(crypto_secretbox_BOXZEROBYTES + box.size)
        val msg = ByteArray(ciphertext.size)
        box.copyInto(ciphertext, crypto_secretbox_BOXZEROBYTES)
        if (ciphertext.size < 32) {
            return null
        }
        if (NaClLowLevel.crypto_secretbox_open(msg, ciphertext, ciphertext.size.toLong(), nonce, key) != 0) {
            return null
        }
        return msg.copyOfRange(crypto_secretbox_ZEROBYTES, msg.size)
    }

    internal fun scalarMult(n: ByteArray, p: ByteArray): ByteArray {
        require(n.size == crypto_scalarmult_SCALARBYTES) { "bad n size" }
        require(p.size == crypto_scalarmult_BYTES) { "bad p size" }
        val q = ByteArray(crypto_scalarmult_BYTES)
        NaClLowLevel.crypto_scalarmult(q, n, p)
        return q
    }

    internal fun boxBefore(publicKey: ByteArray, secretKey: ByteArray): ByteArray {
        checkBoxLengths(publicKey, secretKey)
        val k = ByteArray(crypto_box_BEFORENMBYTES)
        NaClLowLevel.crypto_box_beforenm(k, publicKey, secretKey)
        return k
    }

    fun box(msg: ByteArray, nonce: ByteArray, publicKey: ByteArray, secretKey: ByteArray): ByteArray {
        val k = boxBefore(publicKey, secretKey)
        return secretbox(msg, nonce, k)
    }

    internal fun boxAfter(msg: ByteArray, nonce: ByteArray, key: ByteArray) = secretbox(msg, nonce, key)

    fun boxOpen(msg: ByteArray, nonce: ByteArray, publicKey: ByteArray, secretKey: ByteArray): ByteArray? {
        val k = boxBefore(publicKey, secretKey)
        return secretboxOpen(msg, nonce, k)
    }

    internal fun boxOpenAfter(box: ByteArray, nonce: ByteArray, key: ByteArray) = secretboxOpen(box, nonce, key)

    fun boxKeyPair(): Pair<ByteArray, ByteArray> {
        val pk = ByteArray(crypto_box_PUBLICKEYBYTES)
        val sk = ByteArray(crypto_box_SECRETKEYBYTES)
        NaClLowLevel.crypto_box_keypair(pk, sk)
        return (pk to sk)
    }

    fun boxKeyPairFromSecretKey(secretKey: ByteArray): Pair<ByteArray, ByteArray> {
        require(secretKey.size == crypto_box_SECRETKEYBYTES) { "bad secret key size" }
        val pk = ByteArray(crypto_box_PUBLICKEYBYTES)
        crypto_scalarmult_base(pk, secretKey)
        return (pk to secretKey)
    }


    /////////////////////////////
    //
    // TODO: hash and sign functionality from NaCl is not used here and has not been checked, methods are marked private until more tests can be made
    //
    /////////////////////////////

}