package io.openfuture.chain.block

import io.openfuture.chain.block.validation.BlockValidationProvider
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.crypto.key.NodeKeyHolder
import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.block.PendingBlock
import io.openfuture.chain.domain.block.Signature
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.BlockType
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.property.NodeProperties
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
    private val baseTransactionService: BaseTransactionService<BaseTransaction>,
    private val signatureCollector: SignatureCollector,
    private val keyHolder: NodeKeyHolder,
    private val validationService: BlockValidationProvider,
    private val consensusService: ConsensusService,
    private val clock: NodeClock,
    private val delegateService: DelegateService,
    private val properties: NodeProperties,
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
        if (properties.host == nextProducer.host && properties.port == nextProducer.port) {
            val pendingTransactions
                = baseTransactionService.getPendingFirstWithLimit(consensusProperties.blockCapacity!!)
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

    private fun create(transactions: MutableSet<BaseTransaction>, previousBlock: Block, genesisBlock: GenesisBlock) {
        if (transactions.size != consensusProperties.blockCapacity) {
            return
        }

        val blockType = if (consensusService.isGenesisBlockNeeded()) BlockType.GENESIS else BlockType.MAIN

        val height = previousBlock.height + 1
        val hash = previousBlock.hash
        val time = clock.networkTime()
        val privateKey = keyHolder.getPrivateKey()
        val publicKey = keyHolder.getPublicKey()

        val block = when(blockType) {
            BlockType.MAIN -> {
                MainBlock(
                    privateKey,
                    height,
                    hash,
                    BlockUtils.calculateMerkleRoot(transactions),
                    time,
                    publicKey,
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

}