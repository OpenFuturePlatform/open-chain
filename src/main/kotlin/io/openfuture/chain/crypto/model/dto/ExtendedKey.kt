package io.openfuture.chain.crypto.model.dto

import io.openfuture.chain.crypto.constants.SeedConstant
import io.openfuture.chain.crypto.util.HashUtils
import java.util.*

class ExtendedKey(
    keyHash: ByteArray,
    leftBytes: ByteArray = Arrays.copyOfRange(keyHash, 0, 32),
    rightBytes: ByteArray = Arrays.copyOfRange(keyHash, 32, 64),
    parentKey: ECKey? = null,
    val sequence: Int = 0,
    val depth: Int = 0,
    val parentFingerprint: Int = 0,
    val chainCode: ByteArray = rightBytes,
    val ecKey: ECKey = parentKey?.let { ECKey(leftBytes, it) } ?: ECKey(leftBytes)
) {

    companion object {

        fun root(seed: ByteArray): ExtendedKey {
            val hash = HashUtils.hmacSha512(SeedConstant.SALT.toByteArray(), seed)
            return ExtendedKey(hash)
        }
    }


    /**
     * Accounts with internal/external key chains need to be implemented
     */
    fun derive(sequence: Int) = getChild(sequence)

    private fun getChild(sequenceNumber: Int): ExtendedKey {
        val child = if (sequenceNumber and -0x80000000 == 0) {
            val public = this.ecKey.public
            val child = ByteArray(public.size + 4)
            System.arraycopy(public, 0, child, 0, public.size)
            child[public.size] = (sequenceNumber.ushr(24) and 0xff).toByte()
            child[public.size + 1] = (sequenceNumber.ushr(16) and 0xff).toByte()
            child[public.size + 2] = (sequenceNumber.ushr(8) and 0xff).toByte()
            child[public.size + 3] = (sequenceNumber and 0xff).toByte()
            child
        } else {
            val private = this.ecKey.getPrivate()
            val child = ByteArray(private.size + 5)
            System.arraycopy(private, 0, child, 1, private.size)
            child[private.size + 1] = (sequenceNumber.ushr(24) and 0xff).toByte()
            child[private.size + 2] = (sequenceNumber.ushr(16) and 0xff).toByte()
            child[private.size + 3] = (sequenceNumber.ushr(8) and 0xff).toByte()
            child[private.size + 4] = (sequenceNumber and 0xff).toByte()
            child
        }
        val keyHash = HashUtils.hmacSha512(chainCode, child)
        return ExtendedKey(keyHash = keyHash, sequence = sequenceNumber, depth = this.depth + 1,
            parentFingerprint = getFingerprint(), parentKey = this.ecKey)
    }

    private fun getFingerprint(): Int {
        val keyHash = HashUtils.ripemd160Sha256(ecKey.public)
        var fingerprint = 0
        for (i in 0..3) {
            fingerprint = fingerprint shl 8
            fingerprint = fingerprint or (keyHash[i].toInt() and 0xff)
        }
        return fingerprint
    }

}
