package io.openfuture.chain.service

import io.openfuture.chain.block.TimeSlot
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.MainBlockRepository
import io.openfuture.chain.util.BlockUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultMainBlockService(
    val blockRepository: MainBlockRepository,
    private val clock: NodeClock,
    private val timeSlot: TimeSlot
) : BlockService<MainBlock> {

    @Transactional(readOnly = true)
    override fun getLast(): MainBlock = blockRepository.findFirstByOrderByHeightDesc()
        ?: throw NotFoundException("Last MainBlock block not exist!")

    override fun get(hash: String): MainBlock = blockRepository.findByHash(hash)
        ?: throw NotFoundException("Block with hash:$hash not found ")

    override fun save(block: MainBlock): MainBlock = blockRepository.save(block)

    override fun isValid(block: MainBlock): Boolean {
        val currentTime = clock.networkTime()

        return timeSlot.verifyTimeSlot(currentTime, block) && isMainBlockValid(block)
    }

    private fun isMainBlockValid(block: Block): Boolean {
        val mainBlock = block as MainBlock
        val transactions = mainBlock.transactions

        if (transactions.isEmpty() || !transactionsIsWellFormed(transactions)) {
            return false
        }

        val transactionsMerkleHash = BlockUtils.calculateMerkleRoot(transactions)
        return block.merkleHash == transactionsMerkleHash
    }

    private fun transactionsIsWellFormed(transactions: Set<BaseTransaction>): Boolean {
        val transactionHashes = transactions.map { it.hash }.toSet()
        return transactionHashes.size == transactions.size
    }

}