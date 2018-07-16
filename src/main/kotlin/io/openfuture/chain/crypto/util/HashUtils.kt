package io.openfuture.chain.crypto.util

import org.bouncycastle.crypto.PBEParametersGenerator
import org.bouncycastle.crypto.digests.RIPEMD160Digest
import org.bouncycastle.crypto.digests.SHA512Digest
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.jcajce.provider.digest.Keccak
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object HashUtils {

    private const val SHA256 = "SHA-256"
    private const val HMACSHA512 = "HmacSHA512"
    private const val KEY_SIZE = 512
    private const val ITERATION_COUNT = 2048


    fun doubleSha256(bytes: ByteArray) = sha256(sha256(bytes))

    fun keyHash(bytes: ByteArray): ByteArray {
        val result = ByteArray(20)
        val sha256 = MessageDigest.getInstance(SHA256).digest(bytes)
        val digest = RIPEMD160Digest()
        digest.update(sha256, 0, sha256.size)
        digest.doFinal(result, 0)
        return result
    }

    fun keccak256(bytes: ByteArray): ByteArray {
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

    fun hashPBKDF2(chars: CharArray, salt: ByteArray): ByteArray {
        val generator = PKCS5S2ParametersGenerator(SHA512Digest())
        generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(chars), salt, ITERATION_COUNT)
        val key = generator.generateDerivedMacParameters(KEY_SIZE) as KeyParameter
        return key.key
    }

    fun toHexString(bytes: ByteArray): String = ByteUtils.toHexString(bytes)

    fun fromHexString(input: String): ByteArray = ByteUtils.fromHexString(input)

}