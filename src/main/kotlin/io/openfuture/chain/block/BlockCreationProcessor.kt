package io.openfuture.chain.block

import io.openfuture.chain.block.validation.BlockValidationProvider
import io.openfuture.chain.component.converter.transaction.impl.RewardTransactionEntityConverter
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.crypto.key.NodeKeyHolder
import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.block.PendingBlock
import io.openfuture.chain.domain.block.Signature
import io.openfuture.chain.domain.transaction.data.RewardTransactionData
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.BlockType
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.service.BaseTransactionService
import io.openfuture.chain.service.BlockService
import io.openfuture.chain.service.ConsensusService
import io.openfuture.chain.service.DelegateService
import io.openfuture.chain.util.BlockUtils
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct

@Component
class BlockCreationProcessor(
    private val blockService: BlockService<Block>,
    private val genesisBlockService: BlockService<GenesisBlock>,
    private val baseTransactionService: BaseTransactionService,
    private val signatureCollector: SignatureCollector,
    private val keyHolder: NodeKeyHolder,
    private val validationService: BlockValidationProvider,
    private val consensusService: ConsensusService,
    private val clock: NodeClock,
    private val delegateService: DelegateService,
    private val consensusProperties: ConsensusProperties,
    private val timeSlot: TimeSlot,
    private val scheduler: TaskScheduler,
    private val rewardTransactionEntityConverter: RewardTransactionEntityConverter
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
        if (!SignatureManager.verify(hash, pendingBlock.signature.value, key)) {
            throw IllegalArgumentException("Inbound block's signature is invalid")
        }

        if (!signatureCollector.addSignatureBlock(pendingBlock)) {
            throw IllegalArgumentException("Either signature is already exists, or not related to pending block")
        }
        return signCreatedBlock(block)
    }

    fun fireBlockCreation() {
        val previousBlock = blockService.getLast()
        val genesisBlock = genesisBlockService.getLast()
        val nextProducer = BlockUtils.getBlockProducer(genesisBlock.activeDelegates, previousBlock)
        if (HashUtils.toHexString(keyHolder.getPublicKey()) == nextProducer.publicKey) {
            val pendingTransactions
                = baseTransactionService.getFirstLimitPending(consensusProperties.blockCapacity!!)
            create(pendingTransactions, previousBlock, genesisBlock)
        }
    }

    private fun signCreatedBlock(block: Block): PendingBlock {
        val publicKey = HashUtils.toHexString(keyHolder.getPublicKey())
        val value = SignatureManager.sign(HashUtils.fromHexString(block.hash), keyHolder.getPrivateKey())
        val signature = Signature(value, publicKey)
        val pendingBlock = PendingBlock(block, signature)
        signatureCollector.setPendingBlock(pendingBlock)
        return pendingBlock
    }

    private fun create(pendingTransactions: MutableSet<BaseTransaction>, previousBlock: Block, genesisBlock: GenesisBlock) {
        val blockType = if (consensusService.isGenesisBlockNeeded()) BlockType.GENESIS else BlockType.MAIN

        val height = previousBlock.height + 1
        val hash = previousBlock.hash
        val time = clock.networkTime()
        val privateKey = keyHolder.getPrivateKey()
        val publicKey = keyHolder.getPublicKey()

        val block = when(blockType) {
            BlockType.MAIN -> {
                val transactions = prepareTransactions(pendingTransactions)

                MainBlock(
                    privateKey,
                    height,
                    hash,
                    time,
                    publicKey,
                    HashUtils.calculateMerkleRoot(transactions),
                    transactions
                )
            }
            BlockType.GENESIS -> {
                GenesisBlock(
                    privateKey,
                    height,
                    hash,
                    time,
                    publicKey,
                    genesisBlock.epochIndex + 1,
                    delegateService.getActiveDelegates()
                )
            }
        }

        signCreatedBlock(block)
    }

    private fun prepareTransactions(pendingTransactions: MutableSet<BaseTransaction>): MutableSet<BaseTransaction> {
        val fees = pendingTransactions.map { it.fee }.sum()
        val delegate = delegateService.getByPublicKey(HashUtils.toHexString(keyHolder.getPublicKey()))
        val rewardTransactionData = RewardTransactionData((fees + consensusProperties.rewardBlock!!),
            consensusProperties.feeRewardTx!!, delegate.address, consensusProperties.genesisAddress!!)

        val rewardTransaction = rewardTransactionEntityConverter.toEntity(clock.networkTime(), rewardTransactionData)

        return mutableSetOf(rewardTransaction, *pendingTransactions.toTypedArray())
    }

}