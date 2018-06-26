package io.openfuture.chain.domain.key

import java.util.*

/**
 * @author Alexey Skadorva
 */
class ExtendedKey {

    private var chainCode: ByteArray? = null
    private var ecKey: ECKey? = null
    private var sequence: Int = 0
    private var depth: Int = 0
    private var parentFingerprint: Int = 0


    constructor(keyHash: ByteArray, compressed: Boolean = true, sequence: Int = 0, depth: Int = 0, parentFingerprint: Int = 0, ecKey: ECKey? = null) {

        //key hash left side, private key base
        val l = Arrays.copyOfRange(keyHash, 0, 32).toString()
        //key hash right side, chaincode
        val r = Arrays.copyOfRange(keyHash, 32, 64).toString()
        //r is chainCode bytes
        this.chainCode = r.toByteArray()
        this.sequence = sequence
        this.depth = depth
        this.parentFingerprint = parentFingerprint

        if (ecKey != null) {
            this.ecKey = ECKey(l.toByteArray(), ecKey)
        } else {
            this.ecKey = ECKey(l.toByteArray(), compressed)
        }
    }

    constructor(chainCode: ByteArray, sequence: Int, depth: Int, parentFingerprint: Int, ecKey: ECKey) {
        this.chainCode = chainCode
        this.sequence = sequence
        this.depth = depth
        this.parentFingerprint = parentFingerprint
        this.ecKey = ecKey
    }

    companion object {
        private val xpub = byteArrayOf(0x04, 0x88.toByte(), 0xB2.toByte(), 0x1E.toByte())
        private val xprv = byteArrayOf(0x04, 0x88.toByte(), 0xAD.toByte(), 0xE4.toByte())
    }

    fun derive(i: Int) = ExtendedKey(ByteArray(0))

}