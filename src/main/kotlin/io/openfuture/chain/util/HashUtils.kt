package io.openfuture.chain.util

import java.security.MessageDigest

object HashUtils {

	fun generateHash(bytes: ByteArray): String {
		val instance = MessageDigest.getInstance("SHA-256")
		val digest = instance.digest(bytes)
		return digest.fold("", { str, it -> str + "%02x".format(it) })
	}

	fun next11Bits(bytes: ByteArray, offset: Int): Int {
		val skip = offset / 8
		val lowerBitsToRemove = (3 * 8 - 11) - (offset % 8)
		return (
            (
                (
                    (
                        (
                            bytes[skip].toInt() and 0xff shl 16
                        )
                        or
                        (
                            bytes[skip + 1].toInt() and 0xff shl 8
                        )
                        or
                        (
                            if (lowerBitsToRemove < 8) {
                                bytes[skip + 2].toInt() and 0xff
                            } else {
                                0
                            }
                        )
                    )
                )
                shr lowerBitsToRemove
            )
            and
            (1 shl 11) - 1
		)
	}

}
