package io.openfuture.chain.core.service.block

import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.block.BaseBlock
import io.openfuture.chain.core.repository.BlockRepository
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.MainBlockService
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
    override fun getLast(): BaseBlock {
        return repository.findFirstByOrderByHeightDesc() ?: throw NotFoundException("Last block not found!")
    }

    @Transactional(readOnly = true)
    override fun isExists(hash: String): Boolean {
        val block = repository.findOneByHash(hash)
        return null != block
    }

}