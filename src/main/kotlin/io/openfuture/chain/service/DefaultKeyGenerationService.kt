package io.openfuture.chain.service

import io.openfuture.chain.domain.key.KeyPair
import io.openfuture.chain.util.HashUtils
import org.bouncycastle.asn1.sec.SECNamedCurves
import org.springframework.stereotype.Service
import java.math.BigInteger
import java.util.*

@Service
class DefaultKeyGenerationService: KeyGenerationService {

    companion object {
        private const val rootHmacKey = "Open seed"

        private val curve = SECNamedCurves.getByName("secp256k1")
    }

    override fun generateKeyPair(seed: String): KeyPair {
        val hash = HashUtils.hmacSha256(rootHmacKey.toByteArray(), seed.toByteArray())
        val privateKey = Arrays.copyOfRange(hash, 0, 32)
        val chainCode = Arrays.copyOfRange(hash, 32, 64)
        val publicKey = curve.g.multiply(BigInteger(1, privateKey)).encoded

        return KeyPair(publicKey, privateKey, chainCode, 0)
    }

    override fun generateKeyPair(pair: KeyPair): KeyPair {
        val hashMessage = curve.g.multiply(BigInteger(1, pair.privateKey)).encoded.plus(BigInteger.valueOf(pair.depth + 1L).toByteArray())
        val hash = HashUtils.hmacSha256(pair.chainCode, hashMessage)
        val childPrivateKey = Arrays.copyOfRange(hash, 0, 32)
        val privateKey = BigInteger(1, childPrivateKey).plus(BigInteger(1, pair.privateKey)).mod(curve.n).toByteArray()
        val chainCode = Arrays.copyOfRange(hash, 32, 64)
        val publicKey = curve.g.multiply(BigInteger(1, privateKey)).encoded

        return KeyPair(publicKey, privateKey, chainCode, pair.depth + 1)
    }

}