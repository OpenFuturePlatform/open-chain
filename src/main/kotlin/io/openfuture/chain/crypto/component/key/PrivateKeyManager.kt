package io.openfuture.chain.crypto.component.key

import io.openfuture.chain.crypto.model.dto.ECKey
import io.openfuture.chain.crypto.util.Base58
import io.openfuture.chain.crypto.util.HashUtils
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


    fun exportPrivateKey(key: ECKey): String = Base58.encode(getWifBytes(key))

    fun importPrivateKey(serializedKey: String): ECKey = parseWifBytes(Base58.decode(serializedKey))

    private fun getWifBytes(key: ECKey): ByteArray {
        return key.private?.let {
            val keyBytes = key.getPrivate()

            val extendedKey = ByteArray(keyBytes.size + 2)
            extendedKey[0] = WIF_PREFIX.toByte()
            System.arraycopy(keyBytes, 0, extendedKey, 1, keyBytes.size)
            extendedKey[keyBytes.size + 1] = WIF_POSTFIX.toByte()

            val checkSum = HashUtils.doubleSha256(extendedKey)
            val result = ByteArray(extendedKey.size + CHECKSUM_SIZE)
            System.arraycopy(extendedKey, 0, result, 0, extendedKey.size)
            System.arraycopy(checkSum, 0, result, extendedKey.size, CHECKSUM_SIZE)
            result
        } ?: throw IllegalStateException("Unable to provide WIF if no private key is present")
    }

    private fun parseWifBytes(keyBytes: ByteArray): ECKey {
        validate(keyBytes)
        return ECKey(Arrays.copyOfRange(keyBytes, 1, keyBytes.size - CHECKSUM_SIZE - 1), true)
    }

    private fun validate(bytes: ByteArray) {
        if (bytes.size != WIF_KEY_LENGTH || bytes.size < 4) {
            throw IllegalArgumentException("Invalid key length")
        }
        if (bytes[0] != WIF_PREFIX.toByte()) {
            throw IllegalArgumentException("Invalid key format")
        }
        checkChecksum(bytes)
    }

    private fun checkChecksum(bytes: ByteArray) {
        val keyBytes = Arrays.copyOfRange(bytes, 0, bytes.size - CHECKSUM_SIZE)
        val checksum = Arrays.copyOfRange(bytes, bytes.size - CHECKSUM_SIZE, bytes.size)
        val actualChecksum = Arrays.copyOfRange(HashUtils.doubleSha256(keyBytes), 0, CHECKSUM_SIZE)
        if (!Arrays.equals(checksum, actualChecksum))
            throw IllegalArgumentException("Invalid checksum")
    }

}