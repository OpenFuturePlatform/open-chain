package io.openfuture.chain.service

import io.openfuture.chain.entity.Block
import io.openfuture.chain.exception.NotFoundException
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

    override fun getLast(): Block = blockRepository.findFirstByOrderByHeightDesc()
        ?: throw NotFoundException("Last block not exist!")

    override fun save(block: Block): Block {
       return blockRepository.save(block)
    }

    override fun isValid(block: Block): Boolean = throw NotImplementedException("Method is not implemented")

}