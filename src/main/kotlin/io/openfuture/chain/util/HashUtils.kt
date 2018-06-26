package io.openfuture.chain.util

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

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

	fun hmacSha256(key: ByteArray, message: ByteArray): ByteArray {
		val key = SecretKeySpec(key, "SHA-256")
		val mac = Mac.getInstance("HmacSHA512")
		mac.init(key)
		return mac.doFinal(message)
	}

}