package io.openfuture.chain.core.service

import io.openfuture.chain.core.model.entity.block.TemporaryBlock
import io.openfuture.chain.core.repository.TemporaryBlockRepository
import org.springframework.stereotype.Service

@Service
class DefaultTemporaryBlockService(
    private val repository: TemporaryBlockRepository
) : TemporaryBlockService {

    override fun getByHeightIn(heights: List<Long>): List<TemporaryBlock> = repository.findByHeightIn(heights)

    override fun save(blocks: List<TemporaryBlock>): List<TemporaryBlock> = repository.saveAll(blocks)

    override fun deleteAll() {
        repository.deleteAll()
    }

}