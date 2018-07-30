package io.openfuture.chain.service.block

import io.openfuture.chain.entity.block.Block
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.BlockRepository
import io.openfuture.chain.service.BlockService
import org.springframework.transaction.annotation.Transactional

abstract class DefaultBlockService<T : Block>(
    protected val repository: BlockRepository<T>
) : BlockService<T> {

    @Transactional(readOnly = true)
    override fun getLast(): T = repository.findFirstByOrderByHeightDesc()
        ?: throw NotFoundException("Not found last block")

    @Transactional
    override fun save(block: T): T = repository.save(block)

}