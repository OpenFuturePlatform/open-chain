package io.openfuture.chain.util

import org.bouncycastle.crypto.digests.RIPEMD160Digest
import java.security.MessageDigest

object HashUtils {

    private val SHA256 = "SHA-256"

    fun generateHash(bytes: ByteArray): String {
        val instance = MessageDigest.getInstance(SHA256)
        val digest = instance.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

    fun keyHash(bytes: ByteArray): ByteArray {
        val result = ByteArray(20)
        val sha256 = MessageDigest.getInstance(SHA256).digest(bytes)
        val digest = RIPEMD160Digest()
        digest.update(sha256, 0, sha256.size)
        digest.doFinal(result, 0)
        return result
    }

}
