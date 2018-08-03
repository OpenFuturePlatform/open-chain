package io.openfuture.chain.core.service.block

import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.domain.NetworkBlock
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.transaction.annotation.Transactional

abstract class BaseBlockService(
    protected val blockService: BlockService
) {

    @Transactional(readOnly = true)
    open fun isValid(block: NetworkBlock): Boolean {
        val lastBlock = blockService.getLast()

        return isValidPreviousHash(block, lastBlock)
            && isValidHeight(block, lastBlock)
            && isValidTimeStamp(block, lastBlock)
            && null != block.hash
            && !isValidHash(block.getBytes(), block.hash!!)
            && null != block.signature
            && null != block.publicKey
            && !isValidSignature(block.getBytes(), block.publicKey!!, block.signature!!)
    }

    private fun isValidPreviousHash(block: NetworkBlock, lastBlock: NetworkBlock): Boolean = (block.previousHash == lastBlock.hash)

    private fun isValidTimeStamp(block: NetworkBlock, lastBlock: NetworkBlock): Boolean = (block.timestamp > lastBlock.timestamp)

    private fun isValidHeight(block: NetworkBlock, lastBlock: NetworkBlock): Boolean = (block.height == lastBlock.height + 1)

    private fun isValidHash(byteData: ByteArray, hash: String): Boolean {
        return ByteUtils.toHexString(HashUtils.doubleSha256((byteData))) == hash
    }

    private fun isValidSignature(byteData: ByteArray, publicKey: String, signature: String): Boolean {
        return SignatureUtils.verify(byteData, signature, ByteUtils.fromHexString(publicKey))
    }

}