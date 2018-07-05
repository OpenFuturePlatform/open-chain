package io.openfuture.chain.crypto.util

import org.bouncycastle.pqc.math.linearalgebra.ByteUtils

object AddressUtils {

    private const val PREFIX = "0x"

    fun removePrefix(address: String): String = address.removePrefix(PREFIX)

    fun addPrefix(address: String): String = PREFIX + address

    fun addChecksum(address: String): String {
        val addressHash = ByteUtils.toHexString(HashUtils.keccak256(address.toByteArray()))
        var result = ""
        for (i in 0 until address.length) {
            result += if (Integer.parseInt(addressHash[i].toString(), 16) >= 8) address[i].toUpperCase() else address[i]
        }
        return result
    }

}