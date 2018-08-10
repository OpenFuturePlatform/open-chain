package io.openfuture.chain.core.service.block

import io.openfuture.chain.core.component.BlockCapacityChecker
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.block.BaseBlock
import io.openfuture.chain.core.repository.BlockRepository
import io.openfuture.chain.core.service.BlockService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultBlockService(
    private val repository: BlockRepository<BaseBlock>,
    private val capacityChecker: BlockCapacityChecker
) : BlockService {

    override fun getAvgProductionTime(): Long {
        return capacityChecker.getCapacity()
    }

    @Transactional(readOnly = true)
    override fun getCount(): Long = repository.count()

    @Transactional(readOnly = true)
    override fun getLast(): BaseBlock {
        return repository.findFirstByOrderByHeightDesc() ?: throw NotFoundException("Last block not found!")
    }

    @Transactional(readOnly = true)
    override fun isExists(hash: String): Boolean = repository.findOneByHash(hash)?.let { true } ?: false

}