package io.openfuture.chain.crypto.model.dto

import io.openfuture.chain.crypto.util.AddressUtils
import io.openfuture.chain.crypto.util.HashUtils
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.DLSequence
import org.bouncycastle.asn1.sec.SECNamedCurves
import org.bouncycastle.asn1.sec.SECObjectIdentifiers
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.crypto.signers.HMacDSAKCalculator
import java.math.BigInteger

class ECKey(
    val private: BigInteger?,
    val public: ByteArray
) {

    companion object {
        private const val PRIVATE_KEY_SIZE = 32

        private val curve = SECNamedCurves.getByOID(SECObjectIdentifiers.secp256k1)
        private val params = ECDomainParameters(curve.curve, curve.g, curve.n, curve.h)
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

    fun getAddress(): String = AddressUtils.bytesToAddress(HashUtils.keccak256(public))

    fun sign(hashedMessage: ByteArray): ByteArray {
        if (isPrivateEmpty()) {
            throw IllegalArgumentException("Unable to sign the data. Private key is not provided")
        }

        val signer = ECDSASigner(HMacDSAKCalculator(SHA256Digest()))
        signer.init(true, ECPrivateKeyParameters(private, params))
        val signature = signer.generateSignature(hashedMessage)

        return ECDSASignature(signature[0], signature[1]).encode()
    }

    fun verify(hashedMessage: ByteArray, signature: ByteArray): Boolean {
        val signer = ECDSASigner()
        signer.init(false, ECPublicKeyParameters(curve.curve.decodePoint(public), params))

        val sign = ECDSASignature(ASN1InputStream(signature).use { it.readObject() as DLSequence })

        return signer.verifySignature(hashedMessage, sign.r, sign.s)
    }

}
