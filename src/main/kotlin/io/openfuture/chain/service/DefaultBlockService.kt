package io.openfuture.chain.service

import io.openfuture.chain.entity.block.Block
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.BlockRepository
import org.apache.commons.lang3.NotImplementedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultBlockService(
    private val blockRepository: BlockRepository<Block>
) : BlockService<Block> {

    @Transactional(readOnly = true)
    override fun get(hash: String): Block = blockRepository.findByHash(hash)
        ?: throw NotFoundException("Block with hash = $hash does not exist!")

    @Transactional(readOnly = true)
    override fun getLast(): Block = blockRepository.findFirstByOrderByHeightDesc()
        ?: throw NotFoundException("Last block not exist!")

    @Transactional
    override fun save(block: Block): Block = blockRepository.save(block)

    override fun isValid(block: Block): Boolean = throw NotImplementedException("Method is not implemented")

}