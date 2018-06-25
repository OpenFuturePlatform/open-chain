package io.openfuture.chain.service

import io.openfuture.chain.domain.block.MinedBlockDto
import io.openfuture.chain.entity.Block
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.BlockRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultBlockService(
        private val repository: BlockRepository,
        private val transactionService: TransactionService
) : BlockService {

    @Transactional(readOnly = true)
    override fun count(): Long = repository.count()

    @Transactional(readOnly = true)
    override fun getAll(pageRequest: Pageable): Page<Block> = repository.findAll(pageRequest)

    @Transactional(readOnly = true)
    override fun getLast(): Block = repository.findFirstByOrderByOrderNumberDesc()
            ?: throw NotFoundException("Last block not exist!")

    @Transactional
    override fun save(dto: MinedBlockDto): Block {
        val block = repository.save(Block.of(dto))
        val transactions = dto.transactions.map { transactionService.save(block, it) }
        block.transactions.addAll(transactions)
        return block
    }

}