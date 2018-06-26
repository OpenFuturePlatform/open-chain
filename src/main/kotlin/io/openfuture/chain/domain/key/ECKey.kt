package io.openfuture.chain.domain.key

import org.bouncycastle.asn1.sec.SECNamedCurves
import java.math.BigInteger

class ECKey {

    companion object {
        val curve = SECNamedCurves.getByName("secp256k1")!!
    }

    var private: BigInteger? = null
    var public: ByteArray? = null

    constructor(bytes: ByteArray) : this(bytes, true)

    constructor(left: ByteArray, parent: ECKey) {
        if (parent.hasPrivate()) {
            this.private = BigInteger(1, left).add(parent.private!!).mod(curve.n)
            setPublic(true, null)
        } else {
            throw Error("Support derived ECKey with public key only")
        }
    }

    constructor(bytes: ByteArray, isPrivate: Boolean) {
        if (isPrivate) {
            this.private = BigInteger(1, bytes)
            setPublic(true, null)
        } else {
            setPublic(false, bytes)
        }
    }

    private fun hasPrivate() = null != private

    private fun setPublic(fromPrivate: Boolean, bytes: ByteArray?) {
        public = if (fromPrivate) {
            curve.g.multiply(private).encoded
        } else {
            bytes
        }
    }

}
