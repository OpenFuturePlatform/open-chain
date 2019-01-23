package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.TransactionFooter
import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.RewardTransactionPayload
import io.openfuture.chain.core.repository.RewardTransactionRepository
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.RewardTransactionService
import io.openfuture.chain.core.service.WalletStateService
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
    private val walletStateService: WalletStateService,
    private val consensusProperties: ConsensusProperties,
    private val delegateService: DelegateService,
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
    override fun create(timestamp: Long, fees: Long): RewardTransactionMessage {
        val senderAddress = consensusProperties.genesisAddress!!
        val rewardBlock = consensusProperties.rewardBlock!!
        val bank = walletStateService.getActualBalanceByAddress(senderAddress)
        val reward = fees + if (rewardBlock > bank) bank else rewardBlock
        val fee = 0L
        val publicKey = keyHolder.getPublicKeyAsHexString()
        val delegate = delegateService.getByPublicKey(publicKey)
        val hash = createHash(TransactionHeader(timestamp, fee, senderAddress), RewardTransactionPayload(reward, delegate.address))
        val signature = SignatureUtils.sign(ByteUtils.fromHexString(hash), keyHolder.getPrivateKey())

        return RewardTransactionMessage(timestamp, fee, senderAddress, hash, signature, publicKey, reward, delegate.address)
    }

    @Transactional
    override fun commit(transaction: RewardTransaction) {
        BlockchainLock.writeLock.lock()
        try {
            val persistedTransaction = repository.findOneByFooterHash(transaction.footer.hash)
            if (null != persistedTransaction) {
                return
            }

            repository.save(transaction)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    override fun updateState(message: RewardTransactionMessage) {
        walletStateService.updateBalanceByAddress(message.recipientAddress, message.reward)

        val senderAddress = consensusProperties.genesisAddress!!
        val bank = walletStateService.getActualBalanceByAddress(senderAddress)
        val reward = if (consensusProperties.rewardBlock!! > bank) bank else consensusProperties.rewardBlock!!

        walletStateService.updateBalanceByAddress(senderAddress, -reward)
    }

    @Transactional(readOnly = true)
    override fun verify(message: RewardTransactionMessage): Boolean {
        return try {
            val header = TransactionHeader(message.timestamp, message.fee, message.senderAddress)
            val payload = RewardTransactionPayload(message.reward, message.recipientAddress)
            val footer = TransactionFooter(message.hash, message.senderSignature, message.senderPublicKey)
            super.validateBase(header, payload, footer)
            true
        } catch (e: ValidationException) {
            log.warn(e.message)
            false
        }
    }

    @Transactional
    override fun save(transaction: RewardTransaction) {
        repository.save(transaction)
    }

}