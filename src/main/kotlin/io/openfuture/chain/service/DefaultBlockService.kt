package io.openfuture.chain.service

import io.openfuture.chain.crypto.key.KeyHolder
import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.events.BlockCreationEvent
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.BlockRepository
import io.openfuture.chain.util.BlockUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultBlockService(
    private val blockRepository: BlockRepository,
    private val transactionService: TransactionService,
    private val keyHolder: KeyHolder,
    private val signatureManager: SignatureManager,
    @Value("\${block.capacity}")private val transactionCapacity: Int
) : BlockService {

    @Transactional(readOnly = true)
    override fun get(id: Int): Block = blockRepository.getOne(id)
        ?: throw NotFoundException("Not found id $id")

    @Transactional(readOnly = true)
    override fun getAll(): MutableList<Block> = blockRepository.findAll()

    override fun getLast(): Block = blockRepository.findFirstByOrderByHeightDesc()
        ?: throw NotFoundException("Last block not exist!")

    fun create(transactions: List<Transaction>): Block {
        val previousBlock = getLast()
        val merkleRootHash = BlockUtils.calculateMerkleRoot(transactions)
        val time = System.currentTimeMillis()
        val hash = BlockUtils.calculateHash(previousBlock.hash, merkleRootHash, time, (previousBlock.height + 1))

        val privateKey = keyHolder.getPrivateKey()
        val signature = signatureManager.sign(hash, privateKey)

        return blockRepository
            .save(
                MainBlock(
                    HashUtils.bytesToHexString(hash),
                    previousBlock.height + 1,
                    previousBlock.hash,
                    merkleRootHash,
                    time,
                    signature,
                    transactions
                )
            )
    }

    @EventListener
    fun fireBlockCreation(event: BlockCreationEvent) {
        val pendingTransactions = transactionService.getPendingTransactions()
        if (transactionCapacity == pendingTransactions.size) {
            this.create(pendingTransactions.toList())
        }
    }

}