package io.openfuture.chain.crypto.signature

import org.apache.commons.lang3.RandomStringUtils
import io.openfuture.chain.crypto.domain.ECKey
import io.openfuture.chain.crypto.util.HashUtils
import org.springframework.stereotype.Component
import java.util.*

@Component
class SignatureManager {

    /**
     * Sign data with ECDSA
     *
     * @param data Information to sign
     * @param privateKey Private key of signer
     * @return Base64 encoded signature
     */
    fun sign(data: ByteArray, privateKey: ByteArray): String =
        Base64.getEncoder().encodeToString(ECKey(privateKey).sign(HashUtils.sha256(data)))

    /**
     * Verify ECDSA signature
     *
     * @param data Information that was signed
     * @param signature Base64 encoded signature
     * @return True if valid signature. False otherwise
     */
    fun verify(data: ByteArray, signature: String, publicKey: ByteArray): Boolean {
        val ecKey = ECKey(publicKey, false)
        val decodedSign = Base64.getDecoder().decode(signature)
        return ecKey.verify(HashUtils.sha256(data), decodedSign)
    }

}