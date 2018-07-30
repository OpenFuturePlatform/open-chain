package io.openfuture.chain.consensus.util

import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.consensus.model.dto.transaction.data.BaseTransactionData
import java.nio.ByteBuffer

object TransactionUtils {

    fun createHash(data: BaseTransactionData, publicKey: String, signature: String): String {
        val bytes = getBytes(publicKey.toByteArray(), signature.toByteArray(), data.getBytes())
        return HashUtils.toHexString(HashUtils.doubleSha256(bytes))
    }

    fun createHash(data: BaseTransactionData, publicKey: ByteArray, privateKey: ByteArray): String {
        val signature = getSignature(data, privateKey)
        val bytes = getBytes(publicKey, signature.toByteArray(), data.getBytes())
        return HashUtils.toHexString(HashUtils.doubleSha256(bytes))
    }

    private fun getSignature(data: BaseTransactionData, privateKey: ByteArray): String {
        return SignatureUtils.sign(data.getBytes(), privateKey)
    }

    private fun getBytes(publicKey: ByteArray, signature: ByteArray, data: ByteArray): ByteArray {
        return ByteBuffer.allocate(data.size + publicKey.size + signature.size)
            .put(data)
            .put(publicKey)
            .put(signature)
            .array()
    }

}