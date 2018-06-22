package io.openfuture.chain.util

import io.openfuture.chain.component.seed.SeedGeneratorConstant
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

	fun nextWordsIndex(bytes: ByteArray, offset: Int): Int {
		val skip = offset / SeedGeneratorConstant.BYTE_SIZE
        val lowerBitsToRemove = (MAX_BYTES_TO_READ * SeedGeneratorConstant.BYTE_SIZE
				- SeedGeneratorConstant.WORD_INDEX_SIZE) - (offset % SeedGeneratorConstant.BYTE_SIZE)

		val firstBytePart = bytes[skip].toInt() and BYTE_MASK shl DOUBLE_BYTE_SIZE
		val secondBytePart = bytes[skip + 1].toInt() and BYTE_MASK shl SeedGeneratorConstant.BYTE_SIZE
		var thirdBytePart = 0
		if (lowerBitsToRemove < SeedGeneratorConstant.BYTE_SIZE) {
			thirdBytePart = bytes[skip + 2].toInt() and BYTE_MASK
		}
        return (
			(((firstBytePart or secondBytePart or thirdBytePart)) shr lowerBitsToRemove)
					and (1 shl SeedGeneratorConstant.WORD_INDEX_SIZE) - 1
		)
	}

}
