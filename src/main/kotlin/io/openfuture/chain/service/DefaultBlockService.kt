package io.openfuture.chain.service

import io.openfuture.chain.domain.block.BlockRequest
import io.openfuture.chain.domain.transaction.TransactionRequest
import io.openfuture.chain.entity.Block
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.BlockRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultBlockService (
    private val repository: BlockRepository,
    private val wallerService: WalletService,
    private val transactionService: TransactionService
) : BlockService {

    @Transactional(readOnly = true)
    override fun get(id: Int): Block = repository.getOne(id)
        ?: throw NotFoundException("Not found id $id")

    @Transactional(readOnly = true)
    override fun getAll(): MutableList<Block> =  repository.findAll()

    @Transactional(readOnly = true)
    override fun getLast(): Block = repository.findFirstByOrderByOrderNumberDesc()
        ?: throw NotFoundException("Last block not exist!")

    @Transactional
    override fun save(request: BlockRequest): Block {
        val savedBlock = repository.save(Block.of(request))

        request.transactions.forEach { saveTransactionRequest(savedBlock, it)}

        return savedBlock
    }

    private fun saveTransactionRequest(block: Block, transactionRequest: TransactionRequest) {
        val savedTransaction = transactionService.save(transactionRequest.apply { blockId = block.id })

        wallerService.updateByTransaction(savedTransaction)

        block.transactions.add(savedTransaction)
    }

}