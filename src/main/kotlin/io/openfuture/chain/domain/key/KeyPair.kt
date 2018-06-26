package io.openfuture.chain.domain.key

import java.util.*

/**
 * @author Alexey Skadorva
 */
class KeyPair(
    val publicKey: ByteArray,
    val privateKey: ByteArray,
    val chainCode: ByteArray,
    val depth: Int
)