package io.openfuture.chain.util

import io.openfuture.chain.domain.block.nested.BlockData
import io.openfuture.chain.domain.block.nested.BlockHash

object BlockUtils {

    fun generateHash(blockData: BlockData, nonce: Long): String {
        val bytes = getByteData(blockData, nonce)
        return HashUtils.generateHash(bytes)
    }

    fun isValidHash(blockData: BlockData, blockHash: BlockHash): Boolean {
        val bytes = getByteData(blockData, blockHash.nonce)
        return blockHash.hash == HashUtils.generateHash(bytes)
    }

    fun generateSignature(privateKey: String, blockData: BlockData, blockHash: BlockHash): String {
        val bytes = getByteData(blockData, blockHash.nonce, blockHash.hash)
        return HashUtils.generateSignature(privateKey, bytes)
    }

    fun isValidSignature(publicKey: String, signature: String, blockData: BlockData, blockHash: BlockHash): Boolean {
        val bytes = getByteData(blockData, blockHash.nonce, blockHash.hash)
        return HashUtils.validateSignature(publicKey, signature, bytes)
    }

    private fun getByteData(blockData: BlockData, nonce: Long): ByteArray {
        return getByteData(blockData, nonce, null)
    }

    private fun getByteData(blockData: BlockData, nonce: Long, hash: String?): ByteArray {
        val builder = StringBuilder()
        builder.append(blockData.timestamp)
        builder.append(blockData.orderNumber)
        builder.append(blockData.previousHash)
        builder.append(blockData.merkleHash.hash)
        builder.append(nonce)
        if (null != hash) {
            builder.append(hash)
        }
        return builder.toString().toByteArray()
    }

}