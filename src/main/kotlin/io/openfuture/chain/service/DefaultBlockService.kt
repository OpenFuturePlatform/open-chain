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
    override fun get(hash: String): Block = blockRepository.findByHash(hash)
        ?: throw NotFoundException("Block with hash:$hash not found")

    @Transactional(readOnly = true)
    override fun getLast(): Block = blockRepository.findFirstByOrderByHeightDesc()
        ?: throw NotFoundException("Last block not found!")

    @Transactional(readOnly = true)
    override fun getLastGenesis(): Block {
        TODO("not implemented")

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