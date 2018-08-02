package io.openfuture.chain.core.service.block

import io.openfuture.chain.core.model.entity.block.BaseBlock
import io.openfuture.chain.core.repository.BlockRepository
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.network.domain.NetworkBlock
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

    override fun getProducingSpeed(): Long {
        return 0
    }

    override fun getLastBlock(): NetworkBlock {
        val block = repository.findFirstByOrderByHeightDesc()!!.toMessage()
    }
}