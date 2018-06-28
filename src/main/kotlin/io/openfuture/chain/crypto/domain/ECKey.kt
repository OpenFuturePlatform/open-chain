package io.openfuture.chain.crypto.domain

import io.openfuture.chain.util.HashUtils
import org.bouncycastle.asn1.sec.SECNamedCurves
import org.bouncycastle.asn1.sec.SECObjectIdentifiers
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.math.BigInteger
import java.util.*

class ECKey {

    companion object {
        val CURVE = SECNamedCurves.getByOID(SECObjectIdentifiers.secp256k1)!!
        const val PRIVATE_KEY_SIZE = 32
    }

    var private: BigInteger? = null
    var public: ByteArray? = null

    constructor(bytes: ByteArray) : this(bytes, true)

    constructor(left: ByteArray, parent: ECKey) {
        if (!parent.isPrivateEmpty()) {
            private = BigInteger(1, left).add(parent.private!!).mod(CURVE.n)
            public = CURVE.g.multiply(private).getEncoded(true)
        } else {
            throw Error("Support derived ECKey with public key only")
        }
    }

    constructor(bytes: ByteArray, isPrivate: Boolean) {
        if (isPrivate) {
            this.private = BigInteger(1, bytes)
            public = CURVE.g.multiply(private).getEncoded(true)
        } else {
            public = bytes
        }
    }

    fun getPrivate(): ByteArray {
        if (isPrivateEmpty()) {
            return ByteArray(0)
        }

        val privateInBytes = private!!.toByteArray()

        if (privateInBytes.size == PRIVATE_KEY_SIZE) {
            return privateInBytes
        }

        val tmp = ByteArray(PRIVATE_KEY_SIZE)
        System.arraycopy(privateInBytes, Math.max(0, privateInBytes.size - PRIVATE_KEY_SIZE), tmp,
            Math.max(0, PRIVATE_KEY_SIZE - privateInBytes.size), Math.min(PRIVATE_KEY_SIZE, privateInBytes.size))

        return tmp
    }

    fun isPrivateEmpty(): Boolean {
        return null == private
    }

    fun getAddress(): String {
        val hash = HashUtils.keccakKeyHash(public!!)
        val addressBytes = Arrays.copyOfRange(hash, 0, 20)
        return "0x${ByteUtils.toHexString(addressBytes)}"
    }
}
