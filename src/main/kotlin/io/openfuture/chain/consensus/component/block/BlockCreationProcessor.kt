package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.consensus.model.dto.transaction.data.RewardTransactionData
import io.openfuture.chain.consensus.model.entity.Delegate
import io.openfuture.chain.consensus.model.entity.block.GenesisBlock
import io.openfuture.chain.consensus.model.entity.block.MainBlock
import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.consensus.service.ConsensusService
import io.openfuture.chain.consensus.service.DelegateService
import io.openfuture.chain.consensus.service.GenesisBlockService
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.transaction.Transaction
import io.openfuture.chain.core.model.entity.transaction.UTransaction
import io.openfuture.chain.core.service.CommonBlockService
import io.openfuture.chain.core.service.UCommonTransactionService
import io.openfuture.chain.crypto.component.key.NodeKeyHolder
import io.openfuture.chain.network.component.node.NodeClock
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.annotation.PostConstruct

@Component
class BlockCreationProcessor(
    private val genesisBlockService: GenesisBlockService,
    private val blockService: CommonBlockService,
    private val transactionService: UCommonTransactionService,
    private val keyHolder: NodeKeyHolder,
    private val consensusService: ConsensusService,
    private val clock: NodeClock,
    private val delegateService: DelegateService,
    private val consensusProperties: ConsensusProperties,
    private val timeSlotHelper: TimeSlotHelper
) {

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val nodePublicKey = ByteUtils.toHexString(keyHolder.getPublicKey())

    private var currentTimeSlot: Long = 0

    @PostConstruct
    fun `a chto esly`() {
        while (true) {
            val timeSlot = timeSlotHelper.getSlotNumber()
            if (timeSlot > currentTimeSlot) {
                currentTimeSlot = timeSlot
                executor.submit { fireBlockCreation(timeSlot) }
            }
            Thread.sleep(100)
        }
    }

    private fun fireBlockCreation(timeSlot: Long) {
        val previousBlock = blockService.getLast()
        val genesisBlock = genesisBlockService.getLast()
        val nextProducer = getBlockProducer(genesisBlock.activeDelegates, previousBlock.height, timeSlot)
        if (nodePublicKey == nextProducer.publicKey) {
            create(previousBlock, genesisBlock)
        }
    }

    private fun create(previousBlock: Block, genesisBlock: GenesisBlock) {
        val height = previousBlock.height + 1
        val previousHash = previousBlock.hash
        val time = clock.networkTime()
        val privateKey = keyHolder.getPrivateKey()
        val publicKey = ByteUtils.toHexString(keyHolder.getPublicKey())
        val block = if (consensusService.isGenesisBlockNeeded()) {
            val pendingTransactions = transactionService.getAll()
            val transactions = prepareTransactions(pendingTransactions)
            MainBlock(height, previousHash, time, publicKey, transactions)
        } else {
            GenesisBlock(height, previousHash, time, publicKey, genesisBlock.epochIndex + 1,
                delegateService.getActiveDelegates())
        }
        block.sign(privateKey)
    }

    private fun getBlockProducer(delegates: Set<Delegate>, previousBlockHeight: Long, slotNumber: Long): Delegate {
        val random = Random(previousBlockHeight + slotNumber)
        return delegates.shuffled(random).first()
    }

    private fun prepareTransactions(pendingTransactions: MutableSet<UTransaction>): MutableSet<Transaction> {
        val fees = pendingTransactions.map { it.fee }.sum()
        val delegate = delegateService.getByPublicKey(ByteUtils.toHexString(keyHolder.getPublicKey()))
        val rewardTransactionData = RewardTransactionData((fees + consensusProperties.rewardBlock!!),
            consensusProperties.feeRewardTx!!, delegate.address, consensusProperties.genesisAddress!!)

        val rewardTransaction = rewardTransactionData.toEntity(clock.networkTime(),
            keyHolder.getPublicKey(), keyHolder.getPrivateKey())

        return mutableListOf(rewardTransaction, *pendingTransactions.toTypedArray()).map {
            when (it) {
                is UTransaction -> it.toConfirmed()
                is Transaction -> it
                else -> throw IllegalArgumentException("Unknown type of transaction")
            }
        }.toMutableSet()
    }

}