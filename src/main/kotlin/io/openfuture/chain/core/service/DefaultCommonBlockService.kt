package io.openfuture.chain.core.service

import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.repository.BlockRepository
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultCommonBlockService(
    private val repository: BlockRepository
) : CommonBlockService {

    @Transactional(readOnly = true)
    override fun getLast(): Block = repository.findFirstByOrderByHeightDesc()
        ?: throw NotFoundException("Not found last block")

    @Transactional(readOnly = true)
    override fun get(hash: String): Block = repository.findByHash(hash)
        ?: throw NotFoundException("Not found block with such hash: $hash")

    @Transactional(readOnly = true)
    override fun getBlocksAfterCurrentHash(hash: String): List<Block>? {
        val block = repository.findByHash(hash)

        return block?.let { repository.findByHeightGreaterThan(block.height) }
    }

    @Transactional(readOnly = true)
    override fun isExists(hash: String): Boolean = repository.existsByHash(hash)

    override fun isValid(block: Block): Boolean {
        val lastBlock = getLast()
        return verifyBlockSignature(block)
            && verifyPreviousHash(block, lastBlock)
            && verifyHeight(block, lastBlock)
            && verifyTimestamp(block, lastBlock)
    }

    private fun verifyBlockSignature(block: Block): Boolean
        = SignatureUtils.verify(block.getBytes(), block.signature!!, ByteUtils.fromHexString(block.publicKey))

    private fun verifyPreviousHash(block: Block, lastBlock: Block): Boolean = (block.previousHash == lastBlock.hash)

    private fun verifyTimestamp(block: Block, lastBlock: Block): Boolean = (block.timestamp > lastBlock.timestamp)

    private fun verifyHeight(block: Block, lastBlock: Block): Boolean = (block.height == lastBlock.height + 1)

}