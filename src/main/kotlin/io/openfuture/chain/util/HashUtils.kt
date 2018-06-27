package io.openfuture.chain.util

import org.apache.commons.lang3.StringUtils
import org.bouncycastle.crypto.digests.RIPEMD160Digest
import org.bouncycastle.jcajce.provider.digest.Keccak
import java.security.MessageDigest

object HashUtils {

    private const val SHA256 = "SHA-256"

    fun generateHash(bytes: ByteArray) = generateHashBytes(bytes).fold(StringUtils.EMPTY) { str, it -> str + "%02x".format(it) }

    fun generateHashBytes(bytes: ByteArray) = MessageDigest.getInstance(SHA256).digest(bytes)

    fun genarateDoubleHashBytes(bytes: ByteArray) = generateHashBytes(generateHashBytes(bytes))

    fun keyHash(bytes: ByteArray): ByteArray {
        val result = ByteArray(20)
        val sha256 = MessageDigest.getInstance(SHA256).digest(bytes)
        val digest = RIPEMD160Digest()
        digest.update(sha256, 0, sha256.size)
        digest.doFinal(result, 0)
        return result
    }

    fun keccakKeyHash(bytes: ByteArray): ByteArray {
        val keccak = Keccak.Digest256()
        keccak.update(bytes)
        return keccak.digest()
    }

}
