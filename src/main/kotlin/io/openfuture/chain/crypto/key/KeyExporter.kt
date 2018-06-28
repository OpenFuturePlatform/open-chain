package io.openfuture.chain.crypto.key

import io.openfuture.chain.crypto.domain.ECKey
import io.openfuture.chain.util.Base58
import io.openfuture.chain.util.HashUtils
import org.springframework.stereotype.Component

@Component
class KeyExporter {

    /**
     * Exporting in a Wallet Import Format - a Base58 String representation of private key
     */
    fun exportPrivateKey(key: ECKey): String = Base58.encode(getWIFBytes(key))

    private fun getWIFBytes(key: ECKey): ByteArray {
        val keyBytes = key.getPrivate()

        val extendedKey = ByteArray(keyBytes.size + 2)
        extendedKey[0] = 0x80.toByte()
        System.arraycopy(keyBytes, 0, extendedKey, 1, keyBytes.size)
        extendedKey[keyBytes.size + 1] = 0x01

        val checkSum = HashUtils.generateHashBytes(extendedKey)
        val result = ByteArray(extendedKey.size + 4)
        System.arraycopy(extendedKey, 0, result, 0, extendedKey.size)
        System.arraycopy(checkSum, 0, result, extendedKey.size, 4)
        return result
    }

}