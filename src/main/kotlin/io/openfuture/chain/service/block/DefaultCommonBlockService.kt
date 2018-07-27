package io.openfuture.chain.service.block

import io.openfuture.chain.entity.Block
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.BlockRepository
import io.openfuture.chain.service.CommonBlockService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultCommonBlockService(
    private val repository: BlockRepository<Block>
) : CommonBlockService {

    override fun getLast(): Block = repository.findFirstByOrderByHeightDesc()
        ?: throw NotFoundException("Not found last block")

    override fun get(hash: String): Block = repository.findByHash(hash)
        ?: throw NotFoundException("Not found block with such hash: $hash")

}