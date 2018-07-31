package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.consensus.model.dto.block.BlockApprovalMessage
import io.openfuture.chain.consensus.model.entity.Delegate
import io.openfuture.chain.consensus.model.entity.block.GenesisBlock
import io.openfuture.chain.consensus.model.entity.block.MainBlock
import io.openfuture.chain.consensus.service.GenesisBlockService
import io.openfuture.chain.consensus.service.MainBlockService
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.crypto.component.key.NodeKeyHolder
import io.openfuture.chain.network.domain.NetworkBlockApprovalMessage
import io.openfuture.chain.network.domain.NetworkGenesisBlock
import io.openfuture.chain.network.domain.NetworkMainBlock
import io.openfuture.chain.network.service.NetworkService
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Component

@Component
class DefaultPendingBlockHandler(
    private val timeSlotHelper: TimeSlotHelper,
    private val genesisBlockService: GenesisBlockService,
    private val mainBlockService: MainBlockService,
    private val keyHolder: NodeKeyHolder,
    private val networkService: NetworkService
) : PendingBlockHandler {

    private var observable: Block? = null
    private var timeSlotNumber: Long = 0
    private var stage: ObserverStage = ObserverStage.IDLE

    private val pendingBlocks: MutableSet<Block> = mutableSetOf()
    private val prepareVotes: MutableMap<String, Delegate> = mutableMapOf()
    private val commitVotes: MutableMap<String, MutableList<Delegate>> = mutableMapOf()

    override fun addBlock(block: Block) {
        val blockSlotNumber = timeSlotHelper.getSlotNumber(block.timestamp)
        if (blockSlotNumber > timeSlotNumber) {
            this.reset()
        }
        val slotOwner = timeSlotHelper.getCurrentSlotOwner()
        if (slotOwner.publicKey == block.publicKey) {
            val isValid = when (block) {
                is MainBlock -> mainBlockService.isValid(block)
                is GenesisBlock -> genesisBlockService.isValid(block)
                else -> throw IllegalArgumentException("Unsupported block type")
            }
            if (isValid) {
                pendingBlocks.add(block)
                val networkBlock = when (block) {
                    is MainBlock -> NetworkMainBlock(block)
                    is GenesisBlock -> NetworkGenesisBlock(block)
                    else -> throw IllegalArgumentException("Unsupported block type")
                }
                networkService.broadcast(networkBlock)
            }
        } else {
            return
        }
        if (ObserverStage.IDLE == stage) {
            observable = block
            stage = ObserverStage.PREPARE
            timeSlotNumber = blockSlotNumber
            val publicKey = ByteUtils.toHexString(keyHolder.getPublicKey())
            val prepareMessage = BlockApprovalMessage(ObserverStage.PREPARE, block.height, block.hash, publicKey)
            val message = NetworkBlockApprovalMessage(prepareMessage)
            networkService.broadcast(message)
        }
    }

    override fun handleApproveMessage(message: BlockApprovalMessage) {
        when (message.stage) {
            ObserverStage.PREPARE -> handlePrevote(message)
            ObserverStage.COMMIT -> handleCommit(message)
            ObserverStage.IDLE -> throw IllegalArgumentException("Unacceptable message type")
        }
    }

    private fun handlePrevote(message: BlockApprovalMessage) {
        val delegates = genesisBlockService.getLast().activeDelegates
        val delegate = delegates.find { message.publicKey == it.publicKey } ?: return

        if (message.hash != observable!!.hash) {
            return
        }
        if (!prepareVotes.containsKey(message.publicKey)) {
            prepareVotes[message.publicKey] = delegate
            //broadcast message
        }
        if (prepareVotes.size > (delegates.size - 1) / 3) {
            this.stage = ObserverStage.COMMIT
            val publicKey = ByteUtils.toHexString(keyHolder.getPublicKey())
            val commitMessage = BlockApprovalMessage(ObserverStage.COMMIT, message.height, message.hash, publicKey)
            // broadcast commitMessage
        }
    }

    private fun handleCommit(message: BlockApprovalMessage) {
        val delegates = genesisBlockService.getLast().activeDelegates
        val delegate = delegates.find { message.publicKey == it.publicKey } ?: return

        val blockCommits = commitVotes[message.hash]
        if (null != blockCommits && !blockCommits.contains(delegate)) {
            blockCommits.add(delegate)
            //broadcast message
            if (blockCommits.size> (delegates.size - 1) / 3 * 2) {
                val block = pendingBlocks.find { it.hash == message.hash }
                when (block) {
                    is MainBlock -> mainBlockService.save(block)
                    is GenesisBlock -> genesisBlockService.save(block)
                }
            }
        } else {
            commitVotes[message.hash] = mutableListOf(delegate)
        }
    }

    private fun reset() {
        this.stage = ObserverStage.IDLE
        prepareVotes.clear()
        commitVotes.clear()
        pendingBlocks.clear()
    }

}