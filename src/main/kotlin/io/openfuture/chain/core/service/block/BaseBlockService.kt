package io.openfuture.chain.core.service.block

import io.openfuture.chain.core.model.entity.block.BaseBlock
import io.openfuture.chain.core.model.entity.block.payload.BaseBlockPayload
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.transaction.annotation.Transactional

abstract class BaseBlockService(
    protected val blockService: BlockService
) {

    @Transactional(readOnly = true)
    open fun isValid(block: BaseBlock): Boolean {
        val lastBlock = blockService.getLast()

        return ((isValidPreviousHash(block, lastBlock)
            && isValidHeight(block, lastBlock)
            && isValidTimeStamp(block, lastBlock))
            && !isValidHash(block.getPayload(), block.hash))
            && !isValidSignature(block.getPayload(), block.publicKey, block.signature)
    }

    private fun isValidPreviousHash(block: BaseBlock, lastBlock: BaseBlock): Boolean = (block.getPayload().previousHash == lastBlock.hash)

    private fun isValidTimeStamp(block: BaseBlock, lastBlock: BaseBlock): Boolean = (block.timestamp > lastBlock.timestamp)

    private fun isValidHeight(block: BaseBlock, lastBlock: BaseBlock): Boolean {
        return (block.height == lastBlock.height + 1)
    }

    private fun isValidHash(payload: BaseBlockPayload, hash: String): Boolean {
        return ByteUtils.toHexString(HashUtils.doubleSha256((payload.getBytes()))) == hash
    }

    private fun isValidSignature(payload: BaseBlockPayload, publicKey: String, signature: String): Boolean {
        return SignatureUtils.verify(payload.getBytes(), signature, ByteUtils.fromHexString(publicKey))
    }

}