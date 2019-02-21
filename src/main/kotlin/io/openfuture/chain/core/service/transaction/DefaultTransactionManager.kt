package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.component.StatePool
import io.openfuture.chain.core.component.TransactionThroughput
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.*
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.*
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultTransactionManager(
    private val repository: TransactionRepository<Transaction>,
    private val uRepository: UTransactionRepository<UnconfirmedTransaction>,
    private val throughput: TransactionThroughput,
    private val uDelegateTransactionService: UDelegateTransactionService,
    private val uTransferTransactionService: UTransferTransactionService,
    private val uVoteTransactionService: UVoteTransactionService,
    private val delegateTransactionService: DelegateTransactionService,
    private val transferTransactionService: TransferTransactionService,
    private val voteTransactionService: VoteTransactionService,
    private val rewardTransactionService: RewardTransactionService,
    private val statePool: StatePool
) : TransactionManager {

    override fun getCount(): Long = repository.count()

    override fun getCountByBlock(block: MainBlock): Long = repository.countByBlock(block)

    override fun getUnconfirmedBalanceBySenderAddress(address: String): Long {
        BlockchainLock.readLock.lock()
        try {
            return uRepository.findAllBySenderAddress(address).asSequence().map {
                it.fee + when (it) {
                    is UnconfirmedTransferTransaction -> it.getPayload().amount
                    is UnconfirmedDelegateTransaction -> it.getPayload().amount
                    else -> 0
                }
            }.sum()
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }


    override fun getProducingPerSecond(): Long = throughput.getThroughput()

    override fun getAllUnconfirmedDelegateTransactions(request: PageRequest): List<UnconfirmedDelegateTransaction> =
        uDelegateTransactionService.getAll(request)

    override fun getAllUnconfirmedTransferTransactions(request: PageRequest): List<UnconfirmedTransferTransaction> =
        uTransferTransactionService.getAll(request)

    override fun getAllUnconfirmedVoteTransactions(request: PageRequest): List<UnconfirmedVoteTransaction> =
        uVoteTransactionService.getAll(request)

    override fun getAllTransferTransactions(request: PageRequest): Page<TransferTransaction> =
        transferTransactionService.getAll(request)

    override fun getAllTransferTransactionsByAddress(address: String, request: PageRequest): Page<TransferTransaction> =
        transferTransactionService.getAllByAddress(address, request)

    override fun getAllRewardTransactions(request: PageRequest): Page<RewardTransaction> =
        rewardTransactionService.getAll(request)

    override fun getAllDelegateTransactionsByBlock(block: Block): List<DelegateTransaction> =
        delegateTransactionService.getAllByBlock(block)

    override fun getAllTransferTransactionsByBlock(block: Block): List<TransferTransaction> =
        transferTransactionService.getAllByBlock(block)

    override fun getAllVoteTransactionsByBlock(block: Block): List<VoteTransaction> =
        voteTransactionService.getAllByBlock(block)

    override fun getDelegateTransactionByHash(hash: String): DelegateTransaction =
        delegateTransactionService.getByHash(hash)

    override fun getTransferTransactionByHash(hash: String): TransferTransaction =
        transferTransactionService.getByHash(hash)

    override fun getVoteTransactionByHash(hash: String): VoteTransaction =
        voteTransactionService.getByHash(hash)

    override fun getRewardTransactionByBlock(block: Block): RewardTransaction? =
        rewardTransactionService.getByBlock(block)

    override fun getRewardTransactionByRecipientAddress(address: String): List<RewardTransaction> =
        rewardTransactionService.getByRecipientAddress(address)

    override fun getUnconfirmedVoteBySenderAgainstDelegate(senderAddress: String, delegateKey: String): UnconfirmedVoteTransaction? =
        uVoteTransactionService.getBySenderAgainstDelegate(senderAddress, delegateKey)

    override fun getLastVoteForDelegate(senderAddress: String, delegateKey: String): VoteTransaction =
        voteTransactionService.getLastVoteForDelegate(senderAddress, delegateKey)

    override fun createRewardTransaction(timestamp: Long): RewardTransaction =
        rewardTransactionService.create(timestamp)

    @Suppress("UNCHECKED_CAST")
    @Transactional
    override fun <T : Transaction> commit(tx: T, receipt: Receipt): T = when (tx) {
        is RewardTransaction -> rewardTransactionService.commit(tx, receipt)
        is DelegateTransaction -> delegateTransactionService.commit(tx, receipt)
        is TransferTransaction -> transferTransactionService.commit(tx, receipt)
        is VoteTransaction -> voteTransactionService.commit(tx, receipt)
        else -> throw IllegalStateException("Wrong type")
    } as T

    @Suppress("UNCHECKED_CAST")
    override fun <uT : UnconfirmedTransaction> add(uTx: uT): uT {
        val unconfirmedBalance = getUnconfirmedBalanceBySenderAddress(uTx.senderAddress)
        return when (uTx) {
            is UnconfirmedDelegateTransaction -> uDelegateTransactionService.add(uTx, unconfirmedBalance)
            is UnconfirmedTransferTransaction -> uTransferTransactionService.add(uTx, unconfirmedBalance)
            is UnconfirmedVoteTransaction -> uVoteTransactionService.add(uTx, unconfirmedBalance)
            else -> throw IllegalStateException("Wrong type")
        } as uT
    }

    override fun processTransactions(transactions: List<Transaction>, delegateWallet: String): List<Receipt> {
        val receipts = mutableListOf<Receipt>()
        statePool.clear()

        transactions.forEach {
            val receipt = processTransaction(it, delegateWallet)
            receipts.add(receipt)
        }

        return receipts
    }

    private fun <T : Transaction> processTransaction(tx: T, delegateWallet: String): Receipt = when (tx) {
        is RewardTransaction -> rewardTransactionService.process(tx, delegateWallet)
        is DelegateTransaction -> delegateTransactionService.process(tx, delegateWallet)
        is TransferTransaction -> transferTransactionService.process(tx, delegateWallet)
        is VoteTransaction -> voteTransactionService.process(tx, delegateWallet)
        else -> throw IllegalStateException("Wrong type")
    }

    @Transactional
    override fun deleteBlockTransactions(blockHeights: List<Long>) {
        BlockchainLock.writeLock.lock()
        try {
            repository.deleteAllByBlockHeightIn(blockHeights)
            repository.flush()
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    @Transactional
    override fun deleteUnconfirmedTransactions() {
        BlockchainLock.writeLock.lock()
        try {
            uRepository.deleteAll()
            uRepository.flush()
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

}