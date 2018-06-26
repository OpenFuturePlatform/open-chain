package io.openfuture.chain.domain.key

import org.bouncycastle.asn1.sec.SECNamedCurves
import java.math.BigInteger

class ECKey {

    companion object {
        val curve = SECNamedCurves.getByName("secp256k1")!!
    }

    private var private: BigInteger? = null
    private var public: ByteArray? = null

    constructor(bytes: ByteArray) : this(bytes, true)

    constructor(left: ByteArray, parent: ECKey) {
        if (parent.hasPrivate()) {
            this.private = BigInteger(1, left).add(parent.private!!).mod(curve.getN())
            setPub(true, null)
        } else {
            throw Error("Support derived ECKey with public key only")
        }
    }

    constructor(bytes: ByteArray, isPrivate: Boolean) {
        if (isPrivate) {
            this.private = BigInteger(1, bytes)
            setPub(true, null)
        } else {
            setPub(false, bytes)
        }
    }

    private fun hasPrivate() = null != private

    private fun setPub(fromPrivate: Boolean, bytes: ByteArray?) {
        if (fromPrivate) {
            public = curve.getG().multiply(private).encoded
        } else {
            public = bytes
        }
    }

}
