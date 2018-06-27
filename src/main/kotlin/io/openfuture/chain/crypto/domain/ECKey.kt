package io.openfuture.chain.crypto.domain

import org.bouncycastle.asn1.sec.SECNamedCurves
import org.bouncycastle.asn1.sec.SECObjectIdentifiers
import java.math.BigInteger

class ECKey(
    val private: BigInteger,
    val public: ByteArray
) {

    companion object {
        private const val PRIVATE_KEY_SIZE = 32

        private val curve = SECNamedCurves.getByOID(SECObjectIdentifiers.secp256k1)
    }

    constructor(bytes: ByteArray): this(
        BigInteger(1, bytes),
        curve.g.multiply(BigInteger(1, bytes)).getEncoded(true)
    )

    constructor(left: ByteArray, parent: ECKey): this(
        BigInteger(1, left).add(parent.private).mod(curve.n),
        curve.g.multiply(BigInteger(1, left)).getEncoded(true)
    )

    fun getPrivate(): ByteArray {
        val privateInBytes = private.toByteArray()

        return if (privateInBytes.size == PRIVATE_KEY_SIZE) {
            privateInBytes
        } else {
            val tmp = ByteArray(PRIVATE_KEY_SIZE)
            System.arraycopy(privateInBytes, Math.max(0, privateInBytes.size - PRIVATE_KEY_SIZE), tmp,
                Math.max(0, PRIVATE_KEY_SIZE - privateInBytes.size), Math.min(PRIVATE_KEY_SIZE, privateInBytes.size))
            tmp
        }
    }

}