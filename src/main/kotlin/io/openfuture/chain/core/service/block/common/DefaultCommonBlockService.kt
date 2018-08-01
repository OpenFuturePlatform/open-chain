package io.openfuture.chain.core.service.block.common

import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.block.BaseBlock
import io.openfuture.chain.core.repository.BlockRepository
import io.openfuture.chain.core.service.CommonBlockService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultCommonBlockService(
    private val repository: BlockRepository
) : CommonBlockService {

    @Transactional(readOnly = true)
    override fun getLast(): BaseBlock = repository.findFirstByOrderByHeightDesc()
        ?: throw NotFoundException("Last block not found")

    @Transactional(readOnly = true)
    override fun get(hash: String): BaseBlock = repository.findByHash(hash)
        ?: throw NotFoundException("BaseBlock with hash: $hash not found")

    @Transactional(readOnly = true)
    override fun getBlocksAfterCurrentHash(hash: String): List<BaseBlock>? {
        val block = repository.findByHash(hash)

        return block?.let { repository.findByHeightGreaterThan(block.height) }
    }

    @Transactional(readOnly = true)
    override fun isExists(hash: String): Boolean = repository.existsByHash(hash)

}