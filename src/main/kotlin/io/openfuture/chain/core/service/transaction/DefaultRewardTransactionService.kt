package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.ReceiptResult
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.RewardTransactionPayload
import io.openfuture.chain.core.repository.RewardTransactionRepository
import io.openfuture.chain.core.service.RewardTransactionService
import io.openfuture.chain.core.service.StateManager
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.message.core.RewardTransactionMessage
import io.openfuture.chain.rpc.domain.transaction.request.TransactionPageRequest
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultRewardTransactionService(
    private val repository: RewardTransactionRepository,
    private val consensusProperties: ConsensusProperties,
    private val stateManager: StateManager,
    private val keyHolder: NodeKeyHolder
) : BaseTransactionService(), RewardTransactionService {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DefaultRewardTransactionService::class.java)
    }


    @Transactional(readOnly = true)
    override fun getAll(request: TransactionPageRequest): Page<RewardTransaction> =
        repository.findAll(request.toEntityRequest())

    @Transactional(readOnly = true)
    override fun getByRecipientAddress(address: String): List<RewardTransaction> =
        repository.findAllByPayloadRecipientAddress(address)

    @Transactional(readOnly = true)
    override fun create(timestamp: Long, fees: Long): RewardTransaction {
        val senderAddress = consensusProperties.genesisAddress!!
        val rewardBlock = consensusProperties.rewardBlock!!
        val bank = stateManager.getActualWalletBalanceByAddress(senderAddress)
        val reward = fees + if (rewardBlock > bank) bank else rewardBlock
        val fee = 0L
        val publicKey = keyHolder.getPublicKeyAsHexString()
        val delegate = stateManager.getLastByAddress<DelegateState>(publicKey)
        val hash = RewardTransaction.generateHash(timestamp, fee, senderAddress, reward, delegate.walletAddress)
        val signature = SignatureUtils.sign(ByteUtils.fromHexString(hash), keyHolder.getPrivateKey())

        return RewardTransaction(timestamp, fee, senderAddress, hash, signature, publicKey,
            RewardTransactionPayload(reward, delegate.walletAddress))
    }

    @Transactional
    override fun commit(transaction: RewardTransaction) {
        BlockchainLock.writeLock.lock()
        try {
            val persistedTransaction = repository.findOneByHash(transaction.hash)
            if (null != persistedTransaction) {
                return
            }

            repository.save(transaction)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    override fun process(message: RewardTransactionMessage): Receipt {
        stateManager.updateWalletBalanceByAddress(message.recipientAddress, message.reward)

        val senderAddress = consensusProperties.genesisAddress!!
        val bank = stateManager.getActualWalletBalanceByAddress(senderAddress)
        val reward = if (consensusProperties.rewardBlock!! > bank) bank else consensusProperties.rewardBlock!!

        stateManager.updateWalletBalanceByAddress(senderAddress, -reward)

        return generateReceipt(message)
    }

    @Transactional(readOnly = true)
    override fun verify(message: RewardTransactionMessage): Boolean {
        return try {
            super.validateBase(RewardTransaction.of(message))
            true
        } catch (e: ValidationException) {
            log.warn(e.message)
            false
        }
    }

    private fun generateReceipt(message: RewardTransactionMessage): Receipt {
        val receipt = Receipt(message.hash)
        receipt.setResults(listOf(ReceiptResult(message.senderAddress, message.recipientAddress, message.reward)))
        return receipt
    }

}