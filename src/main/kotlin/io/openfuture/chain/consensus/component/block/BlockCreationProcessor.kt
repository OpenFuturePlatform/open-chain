package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.consensus.model.dto.block.BlockSignature
import io.openfuture.chain.consensus.model.dto.block.PendingBlock
import io.openfuture.chain.consensus.model.dto.transaction.data.RewardTransactionData
import io.openfuture.chain.consensus.model.entity.block.GenesisBlock
import io.openfuture.chain.consensus.model.entity.block.MainBlock
import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.consensus.service.ConsensusService
import io.openfuture.chain.consensus.service.DelegateService
import io.openfuture.chain.consensus.service.GenesisBlockService
import io.openfuture.chain.consensus.util.TransactionUtils
import io.openfuture.chain.consensus.validation.BlockValidationProvider
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.dictionary.BlockType
import io.openfuture.chain.core.model.entity.transaction.Transaction
import io.openfuture.chain.core.model.entity.transaction.UTransaction
import io.openfuture.chain.core.model.entity.transaction.base.BaseTransaction
import io.openfuture.chain.core.service.CommonBlockService
import io.openfuture.chain.core.service.UCommonTransactionService
import io.openfuture.chain.crypto.component.key.NodeKeyHolder
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.component.node.NodeClock
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct

@Component
class BlockCreationProcessor(
    private val commonTransactionService: UCommonTransactionService,
    private val commonBlockService: CommonBlockService,
    private val genesisBlockService: GenesisBlockService,
    private val signatureCollector: SignatureCollector,
    private val keyHolder: NodeKeyHolder,
    private val validationService: BlockValidationProvider,
    private val consensusService: ConsensusService,
    private val clock: NodeClock,
    private val delegateService: DelegateService,
    private val consensusProperties: ConsensusProperties,
    private val timeSlot: TimeSlot,
    private val scheduler: TaskScheduler
) {

    @PostConstruct
    private fun init() {
        val startTime = timeSlot.getSlotTimestamp() + consensusProperties.timeSlotDuration!!
        val startBlockCreationDate = Date(startTime)
        scheduler.scheduleAtFixedRate(
            { fireBlockCreation() },
            startBlockCreationDate,
            consensusProperties.timeSlotDuration!!
        )
    }

    fun approveBlock(pendingBlock: PendingBlock): PendingBlock {
        val block = pendingBlock.block

        if (!validationService.isValid(block)) {
            throw IllegalArgumentException("Inbound block is not valid")
        }

        val hash = HashUtils.fromHexString(block.hash)
        val key = HashUtils.fromHexString(pendingBlock.signature.publicKey)
        if (!SignatureUtils.verify(hash, pendingBlock.signature.value, key)) {
            throw IllegalArgumentException("Inbound block's signature is invalid")
        }

        if (!signatureCollector.addBlockSignature(pendingBlock)) {
            throw IllegalArgumentException("Either signature is already exists, or not related to pending block")
        }
        return signCreatedBlock(block)
    }

    fun fireBlockCreation() {
        val previousBlock = commonBlockService.getLast()
        val genesisBlock = genesisBlockService.getLast()
        val blockTimestamp = previousBlock.timestamp
        val random = Random(blockTimestamp)
        val nextProducer = genesisBlock.activeDelegates.shuffled(random).first()
        if (HashUtils.toHexString(keyHolder.getPublicKey()) == nextProducer.publicKey) {
            val pendingTransactions = commonTransactionService.getAll()
            create(pendingTransactions, previousBlock, genesisBlock)
        }
    }

    private fun signCreatedBlock(block: Block): PendingBlock {
        val publicKey = HashUtils.toHexString(keyHolder.getPublicKey())
        val value = SignatureUtils.sign(HashUtils.fromHexString(block.hash), keyHolder.getPrivateKey())
        val signature = BlockSignature(value, publicKey)
        val pendingBlock = PendingBlock(block, signature)
        signatureCollector.setPendingBlock(pendingBlock)
        return pendingBlock
    }

    private fun create(pendingTransactions: MutableSet<UTransaction>, previousBlock: Block, genesisBlock: GenesisBlock) {
        val blockType = if (consensusService.isGenesisBlockNeeded()) BlockType.GENESIS else BlockType.MAIN

        val height = previousBlock.height + 1
        val hash = previousBlock.hash
        val time = clock.networkTime()
        val privateKey = keyHolder.getPrivateKey()
        val publicKey = keyHolder.getPublicKey()

        val block = when (blockType) {
            BlockType.MAIN -> {
                val transactions = prepareTransactions(pendingTransactions).map {
                    when (it) {
                        is UTransaction -> it.toConfirmed()
                        is Transaction -> it
                        else -> throw IllegalArgumentException("Unknown type of transaction")
                    }
                }.toMutableSet()

                MainBlock(
                    height,
                    hash,
                    time,
                    HashUtils.toHexString(publicKey),
                    TransactionUtils.calculateMerkleRoot(transactions),
                    transactions
                ).sign(privateKey)
            }
            BlockType.GENESIS -> {
                GenesisBlock(
                    height,
                    hash,
                    time,
                    HashUtils.toHexString(publicKey),
                    genesisBlock.epochIndex + 1,
                    delegateService.getActiveDelegates()
                ).sign(privateKey)
            }
        }

        signCreatedBlock(block)
    }

    private fun prepareTransactions(pendingTransactions: MutableSet<UTransaction>): MutableSet<BaseTransaction> {
        val fees = pendingTransactions.map { it.fee }.sum()
        val delegate = delegateService.getByPublicKey(HashUtils.toHexString(keyHolder.getPublicKey()))
        val rewardTransactionData = RewardTransactionData((fees + consensusProperties.rewardBlock!!),
            consensusProperties.feeRewardTx!!, delegate.address, consensusProperties.genesisAddress!!)

        val rewardTransaction = rewardTransactionData.toEntity(clock.networkTime(),
            keyHolder.getPublicKey(), keyHolder.getPrivateKey())

        return mutableSetOf(rewardTransaction, *pendingTransactions.toTypedArray())
    }

}