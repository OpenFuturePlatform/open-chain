package io.openfuture.chain.smartcontract.core.utils

import org.bouncycastle.crypto.digests.RIPEMD160Digest
import org.bouncycastle.jcajce.provider.digest.Keccak
import java.security.MessageDigest

object HashUtils {
    private const val SHA256 = "SHA-256"

    fun sha256(bytes: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance(SHA256)
        digest.update(bytes, 0, bytes.size)
        return digest.digest()
    }

    fun keccak256(bytes: ByteArray): ByteArray {
        val keccak = Keccak.Digest256()
        keccak.update(bytes)
        return keccak.digest()
    }

    fun ripemd160(bytes: ByteArray): ByteArray {
        val digest = RIPEMD160Digest()
        digest.update(bytes, 0, bytes.size)
        val result = ByteArray(digest.digestSize)
        digest.doFinal(result, 0)
        return result
    }

}