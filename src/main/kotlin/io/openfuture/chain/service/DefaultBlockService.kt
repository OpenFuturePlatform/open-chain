package io.openfuture.chain.service

import io.openfuture.chain.entity.Block
import io.openfuture.chain.repository.BlockRepository
import org.apache.commons.lang3.NotImplementedException
import org.springframework.stereotype.Service

@Service
class DefaultBlockService(
    val blockRepository: BlockRepository<Block>
) : BlockService<Block> {

    override fun get(hash: String): Block {
        return blockRepository.findByHash(hash)!!
    }

    override fun findLast(): Block {
        return blockRepository.findFirstByOrderByHeightDesc()!!
    }

    override fun save(block: Block): Block {
       return blockRepository.save(block)
    }

    override fun isValid(block: Block): Boolean = throw NotImplementedException("Method is not implemented")

}