package io.openfuture.chain.service

import io.openfuture.chain.domain.block.BlockRequest
import io.openfuture.chain.entity.Block
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.BlockRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultBlockService (
        private val blockRepository: BlockRepository,
        private val transactionService: TransactionService
) : BlockService {

    @Transactional(readOnly = true)
    override fun get(id: Int): Block = blockRepository.getOne(id)
        ?: throw NotFoundException("Not found id $id")


    @Transactional(readOnly = true)
    override fun getAll(): MutableList<Block> =  blockRepository.findAll()

    override fun getLast(): Block = blockRepository.findFirstByOrderByOrderNumberDesc()
        ?: throw NotFoundException("Last block not exist!")

    @Transactional
    override fun save(request: BlockRequest): Block {
        val block = blockRepository.save(Block.of(request))
        request.transactions.forEach { transactionService.save(block, it) }
        return block
    }

}