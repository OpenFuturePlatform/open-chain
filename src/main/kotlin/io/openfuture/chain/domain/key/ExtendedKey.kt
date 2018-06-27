package io.openfuture.chain.domain.key

import io.openfuture.chain.util.HashUtils
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class ExtendedKey {

    var chainCode: ByteArray
    var ecKey: ECKey
    var sequence: Int = 0
    var depth: Int = 0
    var parentFingerprint: Int = 0

    constructor(
        keyHash: ByteArray,
        sequence: Int = 0,
        depth: Int = 0,
        parentFingerprint: Int = 0,
        ecKey: ECKey? = null
    ) {
        val leftBytes = Arrays.copyOfRange(keyHash, 0, 32)
        val rightBytes = Arrays.copyOfRange(keyHash, 32, 64)
        this.chainCode = rightBytes
        this.sequence = sequence
        this.depth = depth
        this.parentFingerprint = parentFingerprint
        this.ecKey = ecKey?.let { ECKey(leftBytes, it) } ?: ECKey(leftBytes)
    }

    /**
     * Currently supports only simple derivation m/i
     * Accounts with internal/external key chains need to be implemented
     */
    fun derive(i: Int) = getChild(i)

    private fun getChild(i: Int): ExtendedKey {
        val mac = Mac.getInstance("HmacSHA512", "BC")
        val key = SecretKeySpec(chainCode, "HmacSHA512")
        mac.init(key)
        val public = this.ecKey.public!!
        val child = ByteArray(public.size + 4)
        System.arraycopy(public, 0, child, 0, public.size)
        child[public.size] = (i.ushr(24) and 0xff).toByte()
        child[public.size + 1] = (i.ushr(16) and 0xff).toByte()
        child[public.size + 2] = (i.ushr(8) and 0xff).toByte()
        child[public.size + 3] = (i and 0xff).toByte()
        val keyHash = mac.doFinal(child)
        return ExtendedKey(keyHash, i, this.depth + 1, getFingerprint(), this.ecKey)
    }

    /**
     *  RIPE-MD160(SHA256(public key bytes))
     */
    private fun getFingerprint(): Int {
        val keyHash = HashUtils.keyHash(ecKey.public!!)
        var fingerprint = 0
        for (i in 0..3) {
            fingerprint = fingerprint shl 8
            fingerprint = fingerprint or (keyHash[i].toInt() and 0xff)
        }
        return fingerprint
    }

}