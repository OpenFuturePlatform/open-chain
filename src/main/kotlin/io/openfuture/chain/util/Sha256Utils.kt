package io.openfuture.chain.util

import java.security.MessageDigest

object Sha256Utils {

    fun sha256(bytes: ByteArray): ByteArray {
        return sha256(bytes, 0, bytes.size)
    }

    fun sha256(bytes: ByteArray, offset: Int, length: Int): ByteArray {
        val digest = sha256()
        digest.update(bytes, offset, length)
        return digest.digest()
    }

    fun sha256Twice(bytes: ByteArray, offset: Int, length: Int): ByteArray {
        val digest = sha256()
        digest.update(bytes, offset, length)
        digest.update(digest.digest())
        return digest.digest()
    }

    private fun sha256(): MessageDigest {
        return MessageDigest.getInstance("SHA-256")
    }

}
