package io.openfuture.chain.util

import java.security.MessageDigest

object HashUtils {

    fun generateHash(bytes: ByteArray): String {
        val array = sha256(bytes)
        return array.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun sha256(bytes: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(bytes, 0, bytes.size)
        return digest.digest()
    }

    fun generateSignature(privateKey: String, data: ByteArray): String {
        // todo add logic by genereting signature
        return generateHash(privateKey.toByteArray()) // todo temp solution
    }

    fun validateSignature(publicKey: String, signature: String, data: ByteArray): Boolean {
        // todo add logic by validation signature
        return true
    }

    fun getDificultyString(difficulty: Int): String {
        return String(CharArray(difficulty)).replace('\u0000', '0')
    }

}
