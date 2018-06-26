package io.openfuture.chain.domain.key

import java.util.*

class ExtendedKey {

    private var chainCode: ByteArray
    private var ecKey: ECKey
    private var sequence: Int = 0
    private var depth: Int = 0
    private var parentFingerprint: Int = 0

    constructor(
        keyHash: ByteArray,
        sequence: Int = 0,
        depth: Int = 0,
        parentFingerprint: Int = 0,
        ecKey: ECKey? = null
    ) {
        val leftBytes = Arrays.copyOfRange(keyHash, 0, 32)
        val rightBytes = Arrays.copyOfRange(keyHash, 32, 64)
        this.chainCode = rightBytes
        this.sequence = sequence
        this.depth = depth
        this.parentFingerprint = parentFingerprint

        if (ecKey != null) {
            this.ecKey = ECKey(leftBytes, ecKey)
        } else {
            this.ecKey = ECKey(leftBytes)
        }
    }

    //TODO: implement derivation
    fun derive(i: Int) = ExtendedKey(ByteArray(0))

}