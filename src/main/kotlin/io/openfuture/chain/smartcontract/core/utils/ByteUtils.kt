package io.openfuture.chain.smartcontract.core.utils

import org.bouncycastle.pqc.math.linearalgebra.ByteUtils

object ByteUtils {

    fun toHexString(bytes: ByteArray): String = ByteUtils.toHexString(bytes)

    fun fromHexString(hex: String): ByteArray = ByteUtils.fromHexString(hex)

}