package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.RewardTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.repository.RewardTransactionRepository
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.RewardTransactionService
import io.openfuture.chain.core.service.TransactionService
import io.openfuture.chain.core.service.WalletService
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.BlockMessage
import io.openfuture.chain.network.message.core.MainBlockMessage
import io.openfuture.chain.network.message.core.RewardTransactionMessage
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultRewardTransactionService(
    private val repository: RewardTransactionRepository,
    private val transactionService: TransactionService,
    private val walletService: WalletService,
    private val consensusProperties: ConsensusProperties,
    private val delegateService: DelegateService,
    private val keyHolder: NodeKeyHolder
) : RewardTransactionService {

    @Transactional(readOnly = true)
    override fun getAll(request: PageRequest): Page<RewardTransaction> = repository.findAll(request)

    @Transactional(readOnly = true)
    override fun getByRecipientAddress(address: String): List<RewardTransaction> =
        repository.findAllByPayloadRecipientAddress(address)

    @Transactional(readOnly = true)
    override fun create(timestamp: Long, transactions:List<UnconfirmedTransaction>): RewardTransactionMessage {
        val senderAddress = consensusProperties.genesisAddress!!
        val rewardBlock = consensusProperties.rewardBlock!!
        val bank = walletService.getBalanceByAddress(senderAddress)
        val reward = transactions.map { it.fee }.sum() + if (rewardBlock > bank) bank else rewardBlock
        val fee = 0L
        val publicKey = keyHolder.getPublicKey()
        val delegate = delegateService.getByPublicKey(publicKey)

        val payload = RewardTransactionPayload(reward, delegate.address)
        val hash = transactionService.createHash(timestamp, fee, senderAddress, payload)
        val signature = SignatureUtils.sign(ByteUtils.fromHexString(hash), keyHolder.getPrivateKey())

        return RewardTransactionMessage(timestamp, fee, senderAddress, hash, signature, publicKey, reward, delegate.address)
    }

    @Transactional
    override fun toBlock(message: RewardTransactionMessage, block: MainBlock) {
        val transaction = repository.findOneByHash(message.hash)
        if (null != transaction) {
            return
        }

        updateTransferBalance(message.recipientAddress, message.reward)
        repository.save(RewardTransaction.of(message, block))
    }

    @Transactional(readOnly = true)
    override fun verify(blockMessage: PendingBlockMessage): Boolean {
        val uTransactions = blockMessage.getAllTransactions().map { transactionService.getUTransactionByHash(it) }
        val fees = uTransactions.map { it.fee }.sum()

        return verifyBase(blockMessage.rewardTransaction, blockMessage, fees)
    }

    @Transactional(readOnly = true)
    override fun verify(blockMessage: MainBlockMessage): Boolean {
        val fees = blockMessage.getAllTransactions().map { it.fee }.sum()

        return verifyBase(blockMessage.rewardTransaction, blockMessage, fees)
    }

    private fun verifyBase(rewardTransactionMessage: RewardTransactionMessage, blockMessage: BlockMessage, fees: Long): Boolean {
        val payload = RewardTransactionPayload(rewardTransactionMessage.reward, rewardTransactionMessage.recipientAddress)

        return verifyTimestamp(rewardTransactionMessage.timestamp, blockMessage.timestamp)
            && verifyReward(fees, rewardTransactionMessage.reward)
            && verifyHash(rewardTransactionMessage.timestamp, rewardTransactionMessage.fee,
            rewardTransactionMessage.senderAddress, payload, rewardTransactionMessage.hash)
            && verifySignature(rewardTransactionMessage.hash, rewardTransactionMessage.senderSignature,
            rewardTransactionMessage.senderPublicKey)
    }

    private fun verifyTimestamp(txTimestamp: Long, blockTimestamp: Long): Boolean = txTimestamp == blockTimestamp

    private fun verifyReward(fees: Long, reward: Long): Boolean {
        val senderAddress = consensusProperties.genesisAddress!!
        val bank = walletService.getBalanceByAddress(senderAddress)
        val rewardBlock = consensusProperties.rewardBlock!!

        return reward == (fees + if (rewardBlock > bank) bank else rewardBlock)
    }

    private fun verifyHash(timestamp: Long, fee: Long, senderAddress: String, payload: TransactionPayload, hash: String): Boolean =
        transactionService.createHash(timestamp, fee, senderAddress, payload) == hash

    private fun verifySignature(hash: String, signature: String, publicKey: String): Boolean =
        SignatureUtils.verify(ByteUtils.fromHexString(hash), signature, ByteUtils.fromHexString(publicKey))

    private fun updateTransferBalance(to: String, amount: Long) {
        walletService.increaseBalance(to, amount)

        val senderAddress = consensusProperties.genesisAddress!!
        val bank = walletService.getBalanceByAddress(senderAddress)
        val reward = if (consensusProperties.rewardBlock!! > bank) bank else consensusProperties.rewardBlock!!

        walletService.decreaseBalance(consensusProperties.genesisAddress!!, reward)
    }

}