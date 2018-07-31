package io.openfuture.chain.consensus.util

import io.openfuture.chain.consensus.model.dto.transaction.data.BaseTransactionData
import io.openfuture.chain.core.model.entity.transaction.base.BaseTransaction
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.nio.ByteBuffer

object TransactionUtils {

    fun createHash(data: BaseTransactionData, publicKey: String, signature: String): String {
        val bytes = getBytes(publicKey.toByteArray(), signature.toByteArray(), data.getBytes())
        return ByteUtils.toHexString(HashUtils.doubleSha256(bytes))
    }

    fun createHash(data: BaseTransactionData, publicKey: ByteArray, privateKey: ByteArray): String {
        val signature = getSignature(data, privateKey)
        val bytes = getBytes(publicKey, signature.toByteArray(), data.getBytes())
        return ByteUtils.toHexString(HashUtils.doubleSha256(bytes))
    }

    fun calculateMerkleRoot(transactions: Set<BaseTransaction>): String {
        if (transactions.size == 1) {
            return transactions.single().hash
        }
        var previousTreeLayout = transactions.map { it.hash.toByteArray() }
        var treeLayout = mutableListOf<ByteArray>()
        while(previousTreeLayout.size != 2) {
            for (i in 0 until previousTreeLayout.size step 2) {
                val leftHash = previousTreeLayout[i]
                val rightHash = if (i + 1 == previousTreeLayout.size) {
                    previousTreeLayout[i]
                } else {
                    previousTreeLayout[i + 1]
                }
                treeLayout.add(HashUtils.sha256(leftHash + rightHash))
            }
            previousTreeLayout = treeLayout
            treeLayout = mutableListOf()
        }
        return ByteUtils.toHexString(HashUtils.doubleSha256(previousTreeLayout[0] + previousTreeLayout[1]))
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