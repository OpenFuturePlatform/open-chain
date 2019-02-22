package io.openfuture.chain.core.service.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.component.StatePool
import io.openfuture.chain.core.component.TransactionThroughput
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.model.entity.state.AccountState
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.repository.GenesisBlockRepository
import io.openfuture.chain.core.repository.MainBlockRepository
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.core.service.ReceiptService
import io.openfuture.chain.core.service.StateManager
import io.openfuture.chain.core.service.TransactionManager
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.core.sync.SyncMode
import io.openfuture.chain.core.sync.SyncMode.FULL
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.max

@Service
@Transactional(readOnly = true)
class DefaultMainBlockService(
    private val repository: MainBlockRepository,
    private val stateManager: StateManager,
    private val keyHolder: NodeKeyHolder,
    private val throughput: TransactionThroughput,
    private val statePool: StatePool,
    private val consensusProperties: ConsensusProperties,
    private val genesisBlockRepository: GenesisBlockRepository,
    private val transactionManager: TransactionManager,
    private val receiptService: ReceiptService,
    private val eventPublisher: ApplicationEventPublisher
) : DefaultBlockService<MainBlock>(repository), MainBlockService {

    @BlockchainSynchronized
    override fun create(): MainBlock {
        BlockchainLock.readLock.lock()
        try {
            val timestamp = System.currentTimeMillis()
            val lastBlock = getLastBlock()
            val height = lastBlock.height + 1
            val previousHash = lastBlock.hash
            val publicKey = keyHolder.getPublicKeyAsHexString()
            val delegate = stateManager.getByAddress<DelegateState>(publicKey)

            val unconfirmedTransactions = getTransactions()
            val delegateTransactions = mutableListOf<DelegateTransaction>()
            val transferTransactions = mutableListOf<TransferTransaction>()
            val voteTransactions = mutableListOf<VoteTransaction>()

            unconfirmedTransactions.asSequence().forEach {
                when (it) {
                    is UnconfirmedDelegateTransaction -> delegateTransactions.add(DelegateTransaction.of(it))
                    is UnconfirmedTransferTransaction -> transferTransactions.add(TransferTransaction.of(it))
                    is UnconfirmedVoteTransaction -> voteTransactions.add(VoteTransaction.of(it))
                }
            }

            val rewardTransaction = transactionManager.createRewardTransaction(timestamp)

            val transactions = delegateTransactions + transferTransactions + voteTransactions + rewardTransaction

            val receipts = transactionManager.processTransactions(transactions, delegate.walletAddress)
            val states = statePool.getStates()
            val delegateStates = mutableListOf<DelegateState>()
            val accountStates = mutableListOf<AccountState>()
            states.asSequence().forEach {
                when (it) {
                    is DelegateState -> delegateStates.add(it)
                    is AccountState -> accountStates.add(it)
                }
            }
            val stateHashes = states.map { it.hash } as MutableList
            when (lastBlock) {
                is MainBlock -> lastBlock
                is GenesisBlock -> repository.findFirstByHeightLessThanOrderByHeightDesc(lastBlock.height)
                else -> throw IllegalStateException("Wrong type")
            }?.let {
                stateHashes.add(it.getPayload().stateMerkleHash)
            }

            val stateMerkleHash = HashUtils.merkleRoot(stateHashes)
            val transactionMerkleHash = HashUtils.merkleRoot(transactions.map { it.hash })
            val receiptMerkleHash = HashUtils.merkleRoot(receipts.map { it.hash })

            val payload = MainBlockPayload(transactionMerkleHash, stateMerkleHash, receiptMerkleHash,
                listOf(rewardTransaction), voteTransactions, delegateTransactions, transferTransactions,
                delegateStates, accountStates, receipts)

            val hash = Block.generateHash(timestamp, height, previousHash, payload)
            val signature = SignatureUtils.sign(ByteUtils.fromHexString(hash), keyHolder.getPrivateKey())

            return MainBlock(timestamp, height, previousHash, hash, signature, publicKey, payload)
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    @Transactional
    @Synchronized
    override fun add(block: MainBlock) {
        BlockchainLock.writeLock.lock()
        try {
            if (null != repository.findOneByHash(block.hash)) {
                return
            }

            val transactions = block.getPayload().delegateTransactions + block.getPayload().transferTransactions +
                block.getPayload().voteTransactions + block.getPayload().rewardTransactions
            val states = block.getPayload().delegateStates + block.getPayload().accountStates
            val receipts = block.getPayload().receipts

            val savedBlock = save(block)

            stateManager.commit(states)

            transactions.forEach {
                it.block = savedBlock
                val receipt = receipts.find { receipt -> receipt.transactionHash == it.hash }!!
                transactionManager.commit(it, receipt)
            }

            receipts.forEach {
                it.block = savedBlock
                receiptService.commit(it)
            }

            throughput.updateThroughput(transactions.size, savedBlock.height)
        } finally {
            block.getPayload().delegateTransactions.forEach {
                if (it.getPayload().delegateKey == keyHolder.getPublicKeyAsHexString()) {
                    eventPublisher.publishEvent(FULL)
                }
            }
            BlockchainLock.writeLock.unlock()
        }
    }

    override fun getBlocksByEpochIndex(epochIndex: Long, syncMode: SyncMode): List<MainBlock> {
        val genesisBlock = genesisBlockRepository.findOneByPayloadEpochIndex(epochIndex) ?: return emptyList()
        val beginHeight = genesisBlock.height + 1
        val endEpochHeight = beginHeight + consensusProperties.epochHeight!! - 1
        val heights = (beginHeight..endEpochHeight).toList()

        val blocks = repository.findAllByHeightIn(heights)
        if (syncMode == FULL) {
            blocks.forEach {
                val rewardTx = transactionManager.getRewardTransactionByBlock(it)
                it.getPayload().rewardTransactions = if (null != rewardTx) listOf(rewardTx) else listOf()
                it.getPayload().delegateTransactions = transactionManager.getAllDelegateTransactionsByBlock(it)
                it.getPayload().transferTransactions = transactionManager.getAllTransferTransactionsByBlock(it)
                it.getPayload().voteTransactions = transactionManager.getAllVoteTransactionsByBlock(it)
                it.getPayload().receipts = receiptService.getAllByBlock(it)
            }
        }

        val nextGenesisHeight = endEpochHeight + 1
        if (null == repository.findFirstByHeightGreaterThan(nextGenesisHeight)) {
            val lastBlock = blocks.lastOrNull()
            lastBlock?.getPayload()?.delegateStates = stateManager.getAllDelegateStates()
            lastBlock?.getPayload()?.accountStates = stateManager.getAllAccountStates()
        }

        return blocks
    }

    private fun save(block: MainBlock): MainBlock {
        block.getPayload().rewardTransactions = listOf()
        return repository.saveAndFlush(block)
    }

    private fun getTransactions(): List<UnconfirmedTransaction> {
        var result = mutableListOf<UnconfirmedTransaction>()
        var capacity = consensusProperties.blockCapacity!!
        var counter: Int
        var trOffset = 0L
        var delOffset = 0L
        var vOffset = 0L

        do {
            counter = 0
            val trTx = transactionManager.getAllUnconfirmedTransferTransactions(PageRequest(trOffset, max(capacity / 2, 1)))
            counter += trTx.size
            trOffset += trTx.size
            result.addAll(trTx)
            capacity -= trTx.size
            val delTx = transactionManager.getAllUnconfirmedDelegateTransactions(PageRequest(delOffset, max(capacity / 2, 1)))
            counter += delTx.size
            delOffset += delTx.size
            result.addAll(delTx)
            capacity -= delTx.size
            val vTx = transactionManager.getAllUnconfirmedVoteTransactions(PageRequest(vOffset, max(capacity / 2, 1)))
            counter += vTx.size
            vOffset += vTx.size
            result.addAll(vTx)
            capacity -= vTx.size

            result = filterTransactions(result)
            capacity = consensusProperties.blockCapacity!! - result.size
        } while (0 != counter && 0 < capacity)

        return result
    }

    private fun filterTransactions(transactions: List<UnconfirmedTransaction>): MutableList<UnconfirmedTransaction> {
        val result = mutableListOf<UnconfirmedTransaction>()
        val transactionsBySender = transactions.groupBy { it.senderAddress }

        transactionsBySender.forEach {
            var balance = stateManager.getWalletBalanceByAddress(it.key)
            val list = it.value.filter { tx ->
                balance -= when (tx) {
                    is UnconfirmedTransferTransaction -> tx.fee + tx.getPayload().amount
                    is UnconfirmedDelegateTransaction -> tx.fee + tx.getPayload().amount
                    is UnconfirmedVoteTransaction -> tx.fee
                    else -> 0
                }
                0 <= balance
            }
            result.addAll(list)
        }

        return result
    }

}