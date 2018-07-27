package io.openfuture.chain.service

import io.openfuture.chain.block.TimeSlot
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.transaction.RewardTransactionDto
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.*
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.MainBlockRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultMainBlockService(
    val blockRepository: MainBlockRepository,
    private val clock: NodeClock,
    private val timeSlot: TimeSlot,
    private val voteTransactionService: VoteTransactionService,
    private val transferTransactionService: TransferTransactionService,
    private val delegateTransactionService: DelegateTransactionService,
    private val rewardTransactionService: RewardTransactionService
) : BlockService<MainBlock> {

    @Transactional(readOnly = true)
    override fun getLast(): MainBlock = blockRepository.findFirstByOrderByHeightDesc()
        ?: throw NotFoundException("Last MainBlock block not exist!")

    @Transactional(readOnly = true)
    override fun get(hash: String): MainBlock = blockRepository.findByHash(hash)
        ?: throw NotFoundException("Block with hash:$hash not found ")

    @Transactional
    override fun save(block: MainBlock): MainBlock {
        rewardTransactionService.add(RewardTransactionDto(block.transactions.first() as RewardTransaction))

        val savedBlock = blockRepository.save(block)
        val transactions = block.transactions
        for (transaction in transactions) {
            addTransactionToBlock(transaction, savedBlock)
        }
        return savedBlock
    }

    override fun isValid(block: MainBlock): Boolean {
        val currentTime = clock.networkTime()

        return timeSlot.verifyTimeSlot(currentTime, block) && isTransactionsValid(block)
    }

    private fun isTransactionsValid(block: MainBlock): Boolean {
        val transactions = block.transactions

        if (transactions.isEmpty() || !transactionsIsWellFormed(transactions)) {
            return false
        }

        val transactionsMerkleHash = HashUtils.calculateMerkleRoot(transactions)
        return block.merkleHash == transactionsMerkleHash
    }

    private fun transactionsIsWellFormed(transactions: Set<BaseTransaction>): Boolean {
        val transactionHashes = transactions.map { it.hash }.toSet()
        return transactionHashes.size == transactions.size
    }

    private fun addTransactionToBlock(tx: BaseTransaction, block: MainBlock) {
        when (tx) {
            is VoteTransaction -> voteTransactionService.toBlock(tx, block)
            is TransferTransaction -> transferTransactionService.toBlock(tx, block)
            is DelegateTransaction -> delegateTransactionService.toBlock(tx, block)
            is RewardTransaction -> rewardTransactionService.toBlock(tx, block)
            else -> throw IllegalStateException("Unknown transaction type")
        }
    }

}