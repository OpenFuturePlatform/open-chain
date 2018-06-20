package io.openfuture.chain.util

import java.security.MessageDigest
import jdk.nashorn.tools.ShellFunctions.input
import java.security.PrivateKey
import java.security.Signature


object HashUtils {

	fun generateHash(bytes: ByteArray): String {
		val instance = MessageDigest.getInstance("SHA-256")
		val digest = instance.digest(bytes)
		return digest.fold("", { str, it -> str + "%02x".format(it) })
	}

	fun getDificultyString(difficulty: Int): String {
		return String(CharArray(difficulty)).replace('\u0000', '0')
	}

}
