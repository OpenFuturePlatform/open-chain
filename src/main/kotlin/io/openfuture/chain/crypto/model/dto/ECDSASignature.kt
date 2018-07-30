package io.openfuture.chain.crypto.model.dto

import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.DERSequenceGenerator
import org.bouncycastle.asn1.DLSequence
import java.io.ByteArrayOutputStream
import java.math.BigInteger

data class ECDSASignature(
    val r: BigInteger,
    val s: BigInteger
) {

    companion object {
        private const val DER_ENCODED_SIGN_LENGTH = 72
    }


    constructor(sequence: DLSequence) : this(
        (sequence.getObjectAt(0) as ASN1Integer).positiveValue,
        (sequence.getObjectAt(1) as ASN1Integer).positiveValue
    )

    /**
     * DER (Distinguished Encoding Rules) encoding
     */
    fun encode(): ByteArray {
        val bos = ByteArrayOutputStream(DER_ENCODED_SIGN_LENGTH)
        val seq = DERSequenceGenerator(bos)
        seq.addObject(ASN1Integer(r))
        seq.addObject(ASN1Integer(s))
        seq.close()
        return bos.toByteArray()
    }

}