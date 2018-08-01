package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.consensus.component.block.BlockApprovalStage.*
import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.core.util.DictionaryUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.domain.NetworkBlockApproval
import io.openfuture.chain.network.domain.NetworkMainBlock
import io.openfuture.chain.network.service.NetworkService
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Component

@Component
class DefaultPendingBlockHandler(
    private val epochService: EpochService,
    private val mainBlockService: MainBlockService,
    private val keyHolder: NodeKeyHolder,
    private val networkService: NetworkService
) : PendingBlockHandler {

    val pendingBlocks: MutableSet<MainBlock> = mutableSetOf()
    val prepareVotes: MutableMap<String, Delegate> = mutableMapOf()
    val commitVotes: MutableMap<String, MutableList<Delegate>> = mutableMapOf()

    private var observable: MainBlock? = null
    private var timeSlotNumber: Long = 0
    private var stage: BlockApprovalStage = IDLE


    override fun addBlock(block: MainBlock) {
        val blockSlotNumber = epochService.getSlotNumber(block.timestamp)
        if (blockSlotNumber > timeSlotNumber) {
            this.reset()
        }

        pendingBlocks.add(block)
        val slotOwner = epochService.getCurrentSlotOwner()
        if (slotOwner.publicKey == block.publicKey && mainBlockService.isValid(block)) {
            val networkBlock = NetworkMainBlock(block)
            networkService.broadcast(networkBlock)
            if (IDLE == stage) {
                this.observable = block
                this.stage = PREPARE
                this.timeSlotNumber = blockSlotNumber
                val vote = NetworkBlockApproval(PREPARE.getId(), block.height, block.hash, keyHolder.getPublicKey())
                vote.signature = SignatureUtils.sign(vote.getBytes(), keyHolder.getPrivateKey())
                networkService.broadcast(vote)
            }
        }
    }

    override fun handleApproveMessage(message: NetworkBlockApproval) {
        when (DictionaryUtils.valueOf(BlockApprovalStage::class.java, message.stageId)) {
            PREPARE -> handlePrevote(message)
            COMMIT -> handleCommit(message)
            IDLE -> throw IllegalArgumentException("Unacceptable message type")
        }
    }

    private fun handlePrevote(message: NetworkBlockApproval) {
        val delegates = epochService.getDelegates()
        val delegate = delegates.find { message.publicKey == it.publicKey } ?: return

        if (message.hash != observable!!.hash) {
            return
        }
        if (!prepareVotes.containsKey(message.publicKey) && isValidApprovalSignature(message)) {
            prepareVotes[message.publicKey] = delegate
            networkService.broadcast(message)
        }
        if (prepareVotes.size > (delegates.size - 1) / 3) {
            this.stage = COMMIT
            val commit = NetworkBlockApproval(COMMIT.getId(), message.height, message.hash, keyHolder.getPublicKey())
            commit.signature = SignatureUtils.sign(message.getBytes(), keyHolder.getPrivateKey())
            networkService.broadcast(commit)
        }
    }

    private fun handleCommit(message: NetworkBlockApproval) {
        val delegates = epochService.getDelegates()
        val delegate = delegates.find { message.publicKey == it.publicKey } ?: return

        val blockCommits = commitVotes[message.hash]
        if (null != blockCommits && !blockCommits.contains(delegate) && isValidApprovalSignature(message)) {
            blockCommits.add(delegate)
            networkService.broadcast(message)
            if (blockCommits.size > (delegates.size - 1) / 3 * 2) {
                pendingBlocks.find { it.hash == message.hash }?.let { mainBlockService.save(it) }
            }
        } else {
            commitVotes[message.hash] = mutableListOf(delegate)
        }
    }

    private fun reset() {
        this.stage = IDLE
        prepareVotes.clear()
        commitVotes.clear()
        pendingBlocks.clear()
    }

    private fun isValidApprovalSignature(message: NetworkBlockApproval): Boolean
        = SignatureUtils.verify(message.getBytes(), message.signature!!, ByteUtils.fromHexString(message.publicKey))

}