package io.openfuture.chain.service.impl

import io.openfuture.chain.domain.block.BlockRequest
import io.openfuture.chain.entity.Block
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.BlockRepository
import io.openfuture.chain.service.BlockService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author Homza Pavel
 */
@Service
class DefaultBlockService (
        private val blockRepository: BlockRepository
) : BlockService {

    @Transactional(readOnly = true)
    override fun get(id: Int): Block = blockRepository.getOne(id)
        ?: throw NotFoundException("Not found id $id")


    @Transactional(readOnly = true)
    override fun getAll(): MutableList<Block> =  blockRepository.findAll()

    override fun getLast(): Block = blockRepository.findFirstByOrderByOrderNumberDesc()
        ?: throw NotFoundException("Last block not exist!")


    override fun save(request: BlockRequest): Block {
        return blockRepository.save(Block.of(request))
    }

}