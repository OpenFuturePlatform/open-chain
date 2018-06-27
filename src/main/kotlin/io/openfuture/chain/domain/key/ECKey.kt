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
        if (null != parent.private) {
            private = BigInteger(1, left).add(parent.private!!).mod(curve.n)
            public = curve.g.multiply(private).encoded
        } else {
            throw Error("Support derived ECKey with public key only")
        }
    }

    constructor(bytes: ByteArray, isPrivate: Boolean) {
        if (isPrivate) {
            this.private = BigInteger(1, bytes)
            public = curve.g.multiply(private).encoded
        } else {
            public = bytes
        }
    }

}
