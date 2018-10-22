package io.openfuture.chain.smartcontract.utils

import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import kotlin.text.Charsets.UTF_8

object AddressUtils {

    fun generateContractAddress(address: String, nonce: String): String =
        ByteUtils.toHexString(HashUtils.keccak256((address + nonce).toByteArray(UTF_8)))

}