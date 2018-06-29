package io.openfuture.chain.util

import org.apache.commons.lang3.StringUtils
import org.bouncycastle.crypto.digests.RIPEMD160Digest
import org.bouncycastle.jcajce.provider.digest.Keccak
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object HashUtils {

    private const val SHA256 = "SHA-256"
    private const val HMACSHA512 = "HmacSHA512"

    fun generateHash(bytes: ByteArray) = sha256(bytes).fold(StringUtils.EMPTY) { str, it -> str + "%02x".format(it) }

    fun doubleSha256(bytes: ByteArray) = sha256(sha256(bytes))

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

    fun sha256(bytes: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance(SHA256)
        digest.update(bytes, 0, bytes.size)
        return digest.digest()
    }

    fun hmacSha512(key: ByteArray, message: ByteArray): ByteArray {
        val keySpec = SecretKeySpec(key, HMACSHA512)
        val mac = Mac.getInstance(HMACSHA512)
        mac.init(keySpec)
        return mac.doFinal(message)
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