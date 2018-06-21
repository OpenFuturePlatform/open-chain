package io.openfuture.chain.util

import io.openfuture.chain.constant.CryptoConstant
import java.security.MessageDigest

object HashUtils {

	private const val DOUBLE_BYTE_SIZE = 16

	private const val BYTE_MASK = 0xff

	private const val MAX_BYTES_TO_READ = 3

	fun generateHash(bytes: ByteArray): String {
		val instance = MessageDigest.getInstance("SHA-256")
		val digest = instance.digest(bytes)
		return digest.fold("") { str, it -> str + "%02x".format(it) }
	}

	fun next11Bits(bytes: ByteArray, offset: Int): Int {
		val skip = offset / CryptoConstant.BYTE_SIZE
        val lowerBitsToRemove = (MAX_BYTES_TO_READ * CryptoConstant.BYTE_SIZE
				- CryptoConstant.WORD_INDEX_SIZE) - (offset % CryptoConstant.BYTE_SIZE)

		val firstBytePart = bytes[skip].toInt() and (BYTE_MASK shl DOUBLE_BYTE_SIZE)
		val secondBytePart = bytes[skip + 1].toInt() and (BYTE_MASK shl CryptoConstant.BYTE_SIZE)
		var thirdBytePart = 0
		if (lowerBitsToRemove < CryptoConstant.BYTE_SIZE) {
			thirdBytePart = bytes[skip + 2].toInt() and BYTE_MASK
		}
        return (
			(((firstBytePart or secondBytePart or thirdBytePart)) shr lowerBitsToRemove)
					and (1 shl CryptoConstant.WORD_INDEX_SIZE) - 1
		)
	}

}
