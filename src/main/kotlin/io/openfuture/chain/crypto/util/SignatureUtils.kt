package io.openfuture.chain.crypto.util

import io.openfuture.chain.crypto.model.dto.ECKey
import java.util.*

object SignatureUtils {

    /**
     * Sign data with ECDSA
     *
     * @param data Information to sign
     * @param privateKey Private key of signer
     * @return Base64 encoded signature
     */
    fun sign(data: ByteArray, privateKey: ByteArray): String =
        Base64.getEncoder().encodeToString(ECKey(privateKey).sign(data))

    /**
     * Verify ECDSA value
     *
     * @param data Information that was signed
     * @param signature Base64 encoded signature
     * @return True if valid signature. False otherwise
     */
    fun verify(hash: ByteArray, signature: String, publicKey: ByteArray): Boolean {
        val ecKey = ECKey(publicKey, false)
        val decodedSign = Base64.getDecoder().decode(signature)
        return ecKey.verify(hash, decodedSign)
    }

}