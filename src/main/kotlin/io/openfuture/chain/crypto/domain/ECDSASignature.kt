package io.openfuture.chain.crypto.domain

import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.DERSequenceGenerator
import org.bouncycastle.asn1.DLSequence
import java.io.ByteArrayOutputStream
import java.math.BigInteger

data class ECDSASignature(
    val r: BigInteger,
    val s: BigInteger
) {

    constructor(sequence: DLSequence) : this(
        (sequence.getObjectAt(0) as ASN1Integer).positiveValue,
        (sequence.getObjectAt(1) as ASN1Integer).positiveValue
    )

    fun toDER(): ByteArray {
        val bos = ByteArrayOutputStream(72)
        val seq = DERSequenceGenerator(bos)
        seq.addObject(ASN1Integer(r))
        seq.addObject(ASN1Integer(s))
        seq.close()
        return bos.toByteArray()
    }

}