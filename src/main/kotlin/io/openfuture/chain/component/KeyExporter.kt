package io.openfuture.chain.component

import io.openfuture.chain.domain.key.ECKey
import io.openfuture.chain.util.ByteUtils
import io.openfuture.chain.util.HashUtils
import org.springframework.stereotype.Component

@Component
class KeyExporter {

    /**
     * Exporting in a Wallet Import Format - a Base58 String representation of private key
     */
    fun exportPrivateKey(key: ECKey): String = ByteUtils.toBase58(getWIFBytes(key))

    private fun getWIFBytes(key: ECKey): ByteArray {
        return key.private?.let {
            val keyBytes = key.private!!.toByteArray()
            val ek = ByteArray(keyBytes.size + 2)
            ek[0] = 0x80.toByte()
            System.arraycopy(keyBytes, 0, ek, 1, keyBytes.size)
            ek[keyBytes.size + 1] = 0x01
            val wif = ByteArray(keyBytes.size + 6)
            val hash = HashUtils.generateHashBytes(ek)
            System.arraycopy(ek, 0, wif, 0, ek.size)
            System.arraycopy(hash, 0, wif, ek.size, 4)
            wif
        } ?: throw IllegalStateException("Unable to provide WIF if no private key is present")
    }

}