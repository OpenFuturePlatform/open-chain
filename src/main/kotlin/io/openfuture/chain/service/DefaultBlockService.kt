package io.openfuture.chain.service

import io.openfuture.chain.entity.Block
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.BlockRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultBlockService (
        private val blockRepository: BlockRepository
) : BlockService {

    @Transactional(readOnly = true)
    override fun get(id: Int): Block = blockRepository.getOne(id)
        ?: throw NotFoundException("Not found id $id")


    @Transactional(readOnly = true)
    override fun getAll(): MutableList<Block> =  blockRepository.findAll()

    override fun getLast(): Block = blockRepository.findFirstByOrderByHeightDesc()
        ?: throw NotFoundException("Last block not exist!")

}