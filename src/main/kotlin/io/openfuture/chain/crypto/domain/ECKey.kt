package io.openfuture.chain.crypto.domain

import io.openfuture.chain.util.HashUtils
import org.bouncycastle.asn1.sec.SECNamedCurves
import org.bouncycastle.asn1.sec.SECObjectIdentifiers
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.math.BigInteger
import java.util.*

class ECKey(
    val private: BigInteger?,
    val public: ByteArray
) {

    companion object {
        private const val PRIVATE_KEY_SIZE = 32

        private val curve = SECNamedCurves.getByOID(SECObjectIdentifiers.secp256k1)
    }

    constructor(bytes: ByteArray) : this(
        BigInteger(1, bytes),
        curve.g.multiply(BigInteger(1, bytes)).getEncoded(true)
    )

    constructor(bytes: ByteArray, parent: ECKey) : this(
        BigInteger(1, bytes).add(parent.private).mod(curve.n),
        curve.g.multiply(BigInteger(1, bytes).add(parent.private).mod(curve.n)).getEncoded(true)
    )

    constructor(bytes: ByteArray, isPrivate: Boolean) : this(
        if (isPrivate) BigInteger(1, bytes) else null,
        if (isPrivate) curve.g.multiply(BigInteger(1, bytes)).getEncoded(true) else bytes
    )

    fun getPrivate(): ByteArray {
        if (null == private) {
            return ByteArray(0)
        }
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

    fun isPrivateEmpty() = null == private

    fun getAddress(): String {
        val hash = HashUtils.keccakKeyHash(public)
        val addressBytes = Arrays.copyOfRange(hash, 0, 20)
        return "0x${ByteUtils.toHexString(addressBytes)}"
    }

}
