package io.openfuture.chain.service

import io.openfuture.chain.crypto.key.KeyHolder
import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.*
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

    private var activeDelegates = emptyList<String>()

    @Transactional(readOnly = true)
    override fun get(id: Int): Block = blockRepository.getOne(id)
        ?: throw NotFoundException("Not found id $id")

    @Transactional(readOnly = true)
    override fun getAll(): MutableList<Block> = blockRepository.findAll()

    @Transactional(readOnly = true)
    override fun getLast(): Block = blockRepository.findFirstByOrderByHeightDesc()
        ?: throw NotFoundException("Last block not exist!")

    @Transactional(readOnly = true)
    fun getLastGenesisBlock(): GenesisBlock = blockRepository.findFirstByVersion(BlockVersion.GENESIS.version) as? GenesisBlock
        ?: throw NotFoundException("Last Genesis block not exist!")

    private fun create(transactions: List<Transaction>, previousBlock: Block): Block {
        val merkleRootHash = BlockUtils.calculateMerkleRoot(transactions)
        val time = System.currentTimeMillis()
        val hash = BlockUtils.calculateHash(previousBlock.hash, merkleRootHash, time, (previousBlock.height + 1))

        val privateKey = keyHolder.getPrivateKey()
        val signature = signatureManager.sign(hash, privateKey)

        return blockRepository
            .save(MainBlock(
                    HashUtils.bytesToHexString(hash),
                    previousBlock.height + 1,
                    previousBlock.hash,
                    merkleRootHash,
                    time,
                    signature,
                    transactions)
            )
    }

    @EventListener
    fun fireBlockCreation(event: BlockCreationEvent) {
        val pendingTransactions = transactionService.getPendingTransactions()
        if (transactionCapacity == pendingTransactions.size) {
            val delegates = if (activeDelegates.isEmpty()) {
                getLastGenesisBlock().activeDelegateKeys
            } else {
                activeDelegates
            }.toList()
            val publicKey = HashUtils.bytesToHexString(keyHolder.getPublicKey())
            val previousBlock = getLast()
            if (publicKey == BlockUtils.getBlockProducer(delegates, previousBlock)) {
                this.create(pendingTransactions, previousBlock)
            }
        }
    }

}