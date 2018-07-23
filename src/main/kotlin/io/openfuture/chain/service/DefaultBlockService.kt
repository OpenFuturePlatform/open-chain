package io.openfuture.chain.service

import io.openfuture.chain.entity.block.Block
import io.openfuture.chain.entity.block.GenesisBlock
import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.BlockRepository
import io.openfuture.chain.repository.GenesisBlockRepository
import io.openfuture.chain.repository.MainBlockRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultBlockService(
    private val blockRepository: BlockRepository<Block>,
    private val mainBlockRepository: MainBlockRepository,
    private val genesisBlockRepository: GenesisBlockRepository,
    private val voteTransactionService: VoteTransactionService,
    private val transferTransactionService: TransferTransactionService
) : BlockService {

    @Transactional(readOnly = true)
    override fun get(hash: String): Block = blockRepository.findByHash(hash)
        ?: throw NotFoundException("Block with hash:$hash not found ")


    @Transactional(readOnly = true)
    override fun getLast(): Block =
        blockRepository.findFirstByOrderByHeightDesc()
            ?: throw NotFoundException("Last block not found!")

    @Transactional(readOnly = true)
    override fun getLastMain(): MainBlock =
        mainBlockRepository.findFirstByOrderByHeightDesc()
            ?: throw NotFoundException("Last Main block not found!")

    @Transactional(readOnly = true)
    override fun getLastGenesis(): GenesisBlock =
        genesisBlockRepository.findFirstByOrderByHeightDesc()
            ?: throw NotFoundException("Last Genesis block not exist!")

    @Transactional
    override fun save(block: MainBlock): MainBlock {
        val savedBlock = mainBlockRepository.save(block)
        val transactions = block.transactions
        for (transaction in transactions) {
            addTransactionToBlock(transaction, savedBlock)
        }
        return savedBlock
    }

    @Transactional
    override fun save(block: GenesisBlock): GenesisBlock {
        return genesisBlockRepository.save(block)
    }

    private fun addTransactionToBlock(tx: BaseTransaction, block: MainBlock) {
        when (tx) {
            is VoteTransaction -> voteTransactionService.addToBlock(tx, block)
            is TransferTransaction -> transferTransactionService.addToBlock(tx, block)
        }
    }

}