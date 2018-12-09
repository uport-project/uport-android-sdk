package me.uport.knacl

import me.uport.knacl.NaClLowLevel.crypto_scalarmult_base

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
    val crypto_sign_BYTES = 64
    val crypto_sign_PUBLICKEYBYTES = 32
    val crypto_sign_SECRETKEYBYTES = 64
    val crypto_sign_SEEDBYTES = 32
    val crypto_hash_BYTES = 64

    private fun checkLengths(k: ByteArray, n: ByteArray) {
        require(k.size == crypto_secretbox_KEYBYTES) { "bad key size" }
        require(n.size == crypto_secretbox_NONCEBYTES) { "bad nonce size" }
    }

    private fun checkBoxLengths(pk: ByteArray, sk: ByteArray) {
        require(pk.size == crypto_box_PUBLICKEYBYTES) { "bad public key size" }
        require(sk.size == crypto_box_SECRETKEYBYTES) { "bad secret key size" }
    }

    fun randomBytes(size: Int) = NaClLowLevel.randombytes(size)

    fun secretbox(msg: ByteArray, nonce: ByteArray, key: ByteArray): ByteArray {
        checkLengths(key, nonce)
        val m = ByteArray(crypto_secretbox_ZEROBYTES + msg.size)
        val c = ByteArray(m.size)
        for (i in 0 until msg.size) {
            m[i + crypto_secretbox_ZEROBYTES] = msg[i]
        }
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

    fun scalarMult(n: ByteArray, p: ByteArray): ByteArray {
        require(n.size == crypto_scalarmult_SCALARBYTES) { "bad n size" }
        require(p.size == crypto_scalarmult_BYTES) { "bad p size" }
        val q = ByteArray(crypto_scalarmult_BYTES)
        NaClLowLevel.crypto_scalarmult(q, n, p)
        return q
    }

    fun scalarMultBase(n: ByteArray): ByteArray {
        require(n.size == crypto_scalarmult_SCALARBYTES) { "bad n size" }
        val q = ByteArray(crypto_scalarmult_BYTES)
        NaClLowLevel.crypto_scalarmult_base(q, n)
        return q
    }

    fun boxBefore(publicKey: ByteArray, secretKey: ByteArray): ByteArray {
        checkBoxLengths(publicKey, secretKey)
        val k = ByteArray(crypto_box_BEFORENMBYTES)
        NaClLowLevel.crypto_box_beforenm(k, publicKey, secretKey)
        return k
    }

    fun box(msg: ByteArray, nonce: ByteArray, publicKey: ByteArray, secretKey: ByteArray): ByteArray {
        val k = boxBefore(publicKey, secretKey)
        return secretbox(msg, nonce, k)
    }

    fun boxAfter(msg: ByteArray, nonce: ByteArray, key: ByteArray) = secretbox(msg, nonce, key)

    fun boxOpen(msg: ByteArray, nonce: ByteArray, publicKey: ByteArray, secretKey: ByteArray): ByteArray? {
        val k = boxBefore(publicKey, secretKey)
        return secretboxOpen(msg, nonce, k)
    }

    fun boxOpenAfter(box: ByteArray, nonce: ByteArray, key: ByteArray) = secretboxOpen(box, nonce, key)

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
//
//    nacl.box.publicKeyLength = crypto_box_PUBLICKEYBYTES
//    nacl.box.secretKeyLength = crypto_box_SECRETKEYBYTES
//    nacl.box.sharedKeyLength = crypto_box_BEFORENMBYTES
//    nacl.box.nonceLength = crypto_box_NONCEBYTES
//    nacl.box.overheadLength = nacl.secretbox.overheadLength
//
//    nacl.sign = function(msg, secretKey) {
//        checkArrayTypes(msg, secretKey)
//        if (secretKey.length !== crypto_sign_SECRETKEYBYTES)
//            throw new Error('bad secret key size')
//        var signedMsg = new Uint8Array(crypto_sign_BYTES+msg.length)
//        crypto_sign(signedMsg, msg, msg.length, secretKey)
//        return signedMsg
//    }
//
//    nacl.sign.open = function(signedMsg, publicKey) {
//        checkArrayTypes(signedMsg, publicKey)
//        if (publicKey.length !== crypto_sign_PUBLICKEYBYTES)
//            throw new Error('bad public key size')
//        var tmp = new Uint8Array(signedMsg.length)
//        var mlen = crypto_sign_open(tmp, signedMsg, signedMsg.length, publicKey)
//        if (mlen < 0) return null
//        var m = new Uint8Array(mlen)
//        for (var i = 0; i < m.length; i++) m[i] = tmp[i]
//        return m
//    }
//
//    nacl.sign.detached = function(msg, secretKey) {
//        var signedMsg = nacl.sign(msg, secretKey)
//        var sig = new Uint8Array(crypto_sign_BYTES)
//        for (var i = 0; i < sig.length; i++) sig[i] = signedMsg[i]
//        return sig
//    }
//
//    nacl.sign.detached.verify = function(msg, sig, publicKey) {
//        checkArrayTypes(msg, sig, publicKey)
//        if (sig.length !== crypto_sign_BYTES)
//            throw new Error('bad signature size')
//        if (publicKey.length !== crypto_sign_PUBLICKEYBYTES)
//            throw new Error('bad public key size')
//        var sm = new Uint8Array(crypto_sign_BYTES + msg.length)
//        var m = new Uint8Array(crypto_sign_BYTES + msg.length)
//        var i
//        for (i = 0; i < crypto_sign_BYTES; i++) sm[i] = sig[i]
//        for (i = 0; i < msg.length; i++) sm[i+crypto_sign_BYTES] = msg[i]
//        return (crypto_sign_open(m, sm, sm.length, publicKey) >= 0)
//    }
//
//    nacl.sign.keyPair = function() {
//        var pk = new Uint8Array(crypto_sign_PUBLICKEYBYTES)
//        var sk = new Uint8Array(crypto_sign_SECRETKEYBYTES)
//        crypto_sign_keypair(pk, sk)
//        return {publicKey: pk, secretKey: sk}
//    }
//
//    nacl.sign.keyPair.fromSecretKey = function(secretKey) {
//        checkArrayTypes(secretKey)
//        if (secretKey.length !== crypto_sign_SECRETKEYBYTES)
//            throw new Error('bad secret key size')
//        var pk = new Uint8Array(crypto_sign_PUBLICKEYBYTES)
//        for (var i = 0; i < pk.length; i++) pk[i] = secretKey[32+i]
//        return {publicKey: pk, secretKey: new Uint8Array(secretKey)}
//    }
//
//    nacl.sign.keyPair.fromSeed = function(seed) {
//        checkArrayTypes(seed)
//        if (seed.length !== crypto_sign_SEEDBYTES)
//            throw new Error('bad seed size')
//        var pk = new Uint8Array(crypto_sign_PUBLICKEYBYTES)
//        var sk = new Uint8Array(crypto_sign_SECRETKEYBYTES)
//        for (var i = 0; i < 32; i++) sk[i] = seed[i]
//        crypto_sign_keypair(pk, sk, true)
//        return {publicKey: pk, secretKey: sk}
//    }
//
//    nacl.sign.publicKeyLength = crypto_sign_PUBLICKEYBYTES
//    nacl.sign.secretKeyLength = crypto_sign_SECRETKEYBYTES
//    nacl.sign.seedLength = crypto_sign_SEEDBYTES
//    nacl.sign.signatureLength = crypto_sign_BYTES
//
//    nacl.hash = function(msg) {
//        checkArrayTypes(msg)
//        var h = new Uint8Array(crypto_hash_BYTES)
//        crypto_hash(h, msg, msg.length)
//        return h
//    }
//
//    nacl.hash.hashLength = crypto_hash_BYTES
//
//    nacl.verify = function(x, y) {
//        checkArrayTypes(x, y)
//        // Zero length arguments are considered not equal.
//        if (x.length === 0 || y.length === 0) return false
//        if (x.length !== y.length) return false
//        return (vn(x, 0, y, 0, x.length) === 0) ? true : false
//    }
//
//    nacl.setPRNG = function(fn) {
//        randombytes = fn
//    }
}