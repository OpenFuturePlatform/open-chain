package io.openfuture.chain.crypto.key

import io.openfuture.chain.crypto.domain.ECKey
import io.openfuture.chain.util.Base58CoderUtils
import io.openfuture.chain.util.HashUtils
import org.springframework.stereotype.Component
import java.util.*

/**
 * Component for exporting/importing private keys in WIF (Wallet Import Format) - a Base58 String representation
 * of private key
 */
@Component
class PrivateKeyManager {

    companion object {
        private const val WIF_PREFIX = 0x80
        private const val WIF_POSTFIX = 0x01
        private const val WIF_KEY_LENGTH = 38
        private const val CHECKSUM_SIZE = 4
    }

    fun exportPrivateKey(key: ECKey): String = Base58CoderUtils.encode(getWIFBytes(key))

    fun importPrivateKey(serializedKey: String): ECKey = parseWIFBytes(Base58CoderUtils.decode(serializedKey))

    private fun getWIFBytes(key: ECKey): ByteArray {
        return key.private?.let {
            val keyBytes = key.getPrivate()

            val extendedKey = ByteArray(keyBytes.size + 2)
            extendedKey[0] = WIF_PREFIX.toByte()
            System.arraycopy(keyBytes, 0, extendedKey, 1, keyBytes.size)
            extendedKey[keyBytes.size + 1] = WIF_POSTFIX.toByte()

            val checkSum = HashUtils.genarateDoubleHashBytes(extendedKey)
            val result = ByteArray(extendedKey.size + CHECKSUM_SIZE)
            System.arraycopy(extendedKey, 0, result, 0, extendedKey.size)
            System.arraycopy(checkSum, 0, result, extendedKey.size, CHECKSUM_SIZE)
            result
        } ?: throw IllegalStateException("Unable to provide WIF if no private key is present")
    }

    private fun parseWIFBytes(keyBytes: ByteArray): ECKey {
        checkChecksum(keyBytes)
        if (keyBytes.size == WIF_KEY_LENGTH) {
            val key = Arrays.copyOfRange(keyBytes, 1, keyBytes.size - CHECKSUM_SIZE - 1)
            return ECKey(key, true)
        }
        throw IllegalArgumentException("Invalid key length")
    }

    private fun checkChecksum(bytes: ByteArray) {
        val keyBytes = Arrays.copyOfRange(bytes, 0, bytes.size - CHECKSUM_SIZE)
        val checksum = Arrays.copyOfRange(bytes, bytes.size - CHECKSUM_SIZE, bytes.size)
        val actualChecksum = Arrays.copyOfRange(HashUtils.genarateDoubleHashBytes(keyBytes), 0, CHECKSUM_SIZE)
        if (!Arrays.equals(checksum, actualChecksum))
            throw IllegalArgumentException("Invalid checksum")
    }

}