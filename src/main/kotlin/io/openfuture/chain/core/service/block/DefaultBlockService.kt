package io.openfuture.chain.core.service.block

import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.block.BaseBlock
import io.openfuture.chain.core.repository.BlockRepository
import io.openfuture.chain.core.service.BlockService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultBlockService(
    private val repository: BlockRepository<BaseBlock>
) : BlockService {

    @Transactional(readOnly = true)
    override fun getCount(): Long {
        return repository.count()
    }

    @Transactional
    override fun getProducingSpeed(): Long {
        return 0
    }

    @Transactional(readOnly = true)
    override fun getLast(): NetworkBlock {
        val block = repository.findFirstByOrderByHeightDesc() ?: throw NotFoundException("Last block not found!")
        return block.toMessage()
    }

    @Transactional(readOnly = true)
    override fun getBlocksAfterCurrentHash(hash: String): List<NetworkBlock> {
        val block = repository.findOneByHash(hash) ?: return emptyList()
        return repository.findByHeightGreaterThan(block.height).map { it.toMessage() }
    }

}