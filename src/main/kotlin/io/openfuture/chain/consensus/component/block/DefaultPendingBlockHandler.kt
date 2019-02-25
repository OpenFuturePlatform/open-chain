package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.consensus.component.block.BlockApprovalStage.*
import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.service.BlockManager
import io.openfuture.chain.core.service.block.validation.MainBlockValidator
import io.openfuture.chain.core.service.block.validation.pipeline.BlockValidationPipeline
import io.openfuture.chain.core.sync.ChainSynchronizer
import io.openfuture.chain.core.util.DictionaryUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.service.NetworkApiService
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DefaultPendingBlockHandler(
    private val epochService: EpochService,
    private val blockManager: BlockManager,
    private val keyHolder: NodeKeyHolder,
    private val networkService: NetworkApiService,
    private val chainSynchronizer: ChainSynchronizer,
    private val properties: ConsensusProperties,
    private val mainBlockValidator: MainBlockValidator
) : PendingBlockHandler {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DefaultPendingBlockHandler::class.java)
    }

    private val pendingBlocks: MutableSet<PendingBlockMessage> = mutableSetOf()
    private val prepareVotes: MutableMap<String, MutableList<String>> = mutableMapOf()
    private val commits: MutableMap<String, MutableList<String>> = mutableMapOf()

    private val fullValidationPipe: BlockValidationPipeline = BlockValidationPipeline(mainBlockValidator.checkFull())
    private val partialValidationPipe: BlockValidationPipeline = BlockValidationPipeline(arrayOf(
        mainBlockValidator.checkHeight(),
        mainBlockValidator.checkPreviousHash()
    ))

    private var observable: PendingBlockMessage? = null
    private var timeSlotNumber: Long = 0
    private var stage: BlockApprovalStage = IDLE
    private var blockAddedFlag = false


    @BlockchainSynchronized
    @Synchronized
    override fun addBlock(block: PendingBlockMessage) {
        val blockSlotNumber = epochService.getSlotNumber(System.currentTimeMillis())

        if (blockSlotNumber != timeSlotNumber) {
            this.timeSlotNumber = blockSlotNumber
            this.reset()
        }

        if (!pendingBlocks.add(block) || blockAddedFlag) {
            return
        }

        val lastBlock = blockManager.getLast()
        val lastMainBlock = if (lastBlock !is MainBlock) blockManager.getLastMainBlock() else lastBlock
        val blockValid = mainBlockValidator.verify(MainBlock.of(block), lastBlock, lastMainBlock, true, partialValidationPipe)
        if (IDLE == stage && isActiveDelegate() && blockValid) {
            this.stage = PREPARE
            val publicKey = keyHolder.getPublicKeyAsHexString()
            val vote = BlockApprovalMessage(PREPARE.getId(), block.hash, publicKey)
            vote.signature = SignatureUtils.sign(vote.getBytes(), keyHolder.getPrivateKey())
            val votes = prepareVotes.getOrPut(block.hash) { mutableListOf() }
            votes.add(publicKey)
            networkService.broadcast(vote)
            networkService.broadcast(block)
        }
    }

    @BlockchainSynchronized
    @Synchronized
    override fun handleApproveMessage(message: BlockApprovalMessage) {
        if (!epochService.isInIntermission(System.currentTimeMillis())) {
            when (DictionaryUtils.valueOf(BlockApprovalStage::class.java, message.stageId)) {
                PREPARE -> handlePrevote(message)
                COMMIT -> handleCommit(message)
                IDLE -> throw IllegalArgumentException("Unacceptable message type")
            }
        }
    }

    @Synchronized
    override fun resetSlotNumber() {
        timeSlotNumber = 0L
    }

    private fun handlePrevote(message: BlockApprovalMessage) {
        if (!isActiveDelegate()) {
            networkService.broadcast(message)
            return
        }

        if (COMMIT == this.stage) {
            return
        }

        val delegates = epochService.getDelegatesPublicKeys()
        val delegate = delegates.find { it == message.publicKey } ?: return

        if (!isValidApprovalSignature(message)) {
            return
        }

        val votes = prepareVotes.getOrPut(message.hash) { mutableListOf() }

        if (!votes.contains(delegate)) {
            votes.add(delegate)
            networkService.broadcast(message)
            checkPrevote(votes.size, message)
        }
    }

    private fun checkPrevote(size: Int, message: BlockApprovalMessage) {
        if (size > (properties.delegatesCount!!) / 3) {
            val block = pendingBlocks.find { it.hash == message.hash }
            val slotOwner = epochService.getCurrentSlotOwner()
            if (null != block && slotOwner == block.publicKey) {
                val lastBlock = blockManager.getLast()
                val lastMainBlock = if (lastBlock !is MainBlock) blockManager.getLastMainBlock() else lastBlock
                if (mainBlockValidator.verify(MainBlock.of(block), lastBlock, lastMainBlock,true, fullValidationPipe)) {
                    this.observable = block
                    this.stage = COMMIT
                    val publicKey = keyHolder.getPublicKeyAsHexString()
                    val commit = BlockApprovalMessage(COMMIT.getId(), message.hash, publicKey)
                    commit.signature = SignatureUtils.sign(commit.getBytes(), keyHolder.getPrivateKey())
                    val blockCommits = commits.getOrPut(message.hash) {mutableListOf()}
                    blockCommits.add(publicKey)
                    networkService.broadcast(commit)
                }
            }
        }
    }

    private fun handleCommit(message: BlockApprovalMessage) {
        val delegates = epochService.getDelegatesPublicKeys()
        val delegate = delegates.find { it == message.publicKey } ?: return

        val blockCommits = commits.getOrPut(message.hash) { mutableListOf() }

        if (!blockCommits.contains(delegate) && isValidApprovalSignature(message)) {
            networkService.broadcast(message)
            blockCommits.add(delegate)
            checkCommits(blockCommits.size, message)
        }
    }

    private fun checkCommits(size: Int, message: BlockApprovalMessage) {
        if (size > (properties.delegatesCount!! / 2) && !blockAddedFlag) {
            pendingBlocks.find { it.hash == message.hash }?.let {
                val block = MainBlock.of(it)
                if (!chainSynchronizer.isInSync(block) || (it.hash != observable?.hash && isActiveDelegate())) {
                    chainSynchronizer.checkLastBlock()
                    timeSlotNumber = 0
                    reset()
                    return
                }
                blockManager.add(block)
                log.info("Saving main block: height #${it.height}, hash ${it.hash}")
                blockAddedFlag = true
            }
        }
    }

    private fun reset() {
        this.stage = IDLE
        prepareVotes.clear()
        commits.clear()
        pendingBlocks.clear()
        blockAddedFlag = false
    }

    private fun isValidApprovalSignature(message: BlockApprovalMessage): Boolean =
        SignatureUtils.verify(message.getBytes(), message.signature!!, ByteUtils.fromHexString(message.publicKey))

    private fun isActiveDelegate(): Boolean =
        epochService.getDelegatesPublicKeys().contains(keyHolder.getPublicKeyAsHexString())

}