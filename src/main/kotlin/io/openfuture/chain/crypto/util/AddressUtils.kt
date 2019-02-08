package io.openfuture.chain.crypto.util

import org.apache.commons.lang3.StringUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.util.*

object AddressUtils {

    private const val PREFIX = "0x"
    private const val ADDRESS_SIZE = 20


    fun removePrefix(address: String): String = address.removePrefix(PREFIX)

    fun addChecksum(address: String): String {
        val addressHash = ByteUtils.toHexString(HashUtils.keccak256(address.toByteArray()))
        var result = StringUtils.EMPTY
        for (i in 0 until address.length) {
            result += if (Integer.parseInt(addressHash[i].toString(), 16) >= 8) address[i].toUpperCase() else address[i]
        }
        return result
    }

    fun bytesToAddress(bytes: ByteArray): String {
        val address = ByteUtils.toHexString(Arrays.copyOfRange(bytes, 0, ADDRESS_SIZE))
        return PREFIX + addChecksum(address)
    }

}