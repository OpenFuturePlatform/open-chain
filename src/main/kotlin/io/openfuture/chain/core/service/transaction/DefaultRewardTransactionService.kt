package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.RewardTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.repository.RewardTransactionRepository
import io.openfuture.chain.core.service.RewardTransactionService
import io.openfuture.chain.core.service.TransactionService
import io.openfuture.chain.core.service.WalletService
import io.openfuture.chain.core.util.TransactionUtils
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
    private val consensusProperties: ConsensusProperties
) : RewardTransactionService {

    @Transactional(readOnly = true)
    override fun getAll(request: PageRequest): Page<RewardTransaction> = repository.findAll(request)

    @Transactional(readOnly = true)
    override fun getByRecipientAddress(address: String): List<RewardTransaction> =
        repository.findAllByPayloadRecipientAddress(address)

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
    override fun isValid(blockMessage: PendingBlockMessage): Boolean {
        val uTransactions = blockMessage.getAllTransactions().map { transactionService.getUTransactionByHash(it) }
        val fees = uTransactions.map { it.fee }.sum()

        return isValidBase(blockMessage.rewardTransaction, blockMessage, fees)
    }

    @Transactional(readOnly = true)
    override fun isValid(blockMessage: MainBlockMessage): Boolean {
        val fees = blockMessage.getAllTransactions().map { it.fee }.sum()

        return isValidBase(blockMessage.rewardTransaction, blockMessage, fees)
    }

    private fun isValidBase(rewardTransactionMessage: RewardTransactionMessage, blockMessage: BlockMessage, fees: Long): Boolean {
        val payload = RewardTransactionPayload(rewardTransactionMessage.reward, rewardTransactionMessage.recipientAddress)

        return isValidTimestamp(rewardTransactionMessage.timestamp, blockMessage.timestamp)
            && isValidReward(fees, rewardTransactionMessage.reward)
            && isValidHash(rewardTransactionMessage.timestamp, rewardTransactionMessage.fee,
            rewardTransactionMessage.senderAddress, payload, rewardTransactionMessage.hash)
            && isValidSignature(rewardTransactionMessage.hash, rewardTransactionMessage.senderSignature,
            rewardTransactionMessage.senderPublicKey)
    }

    private fun isValidTimestamp(txTimestamp: Long, blockTimestamp: Long): Boolean = txTimestamp == blockTimestamp

    private fun isValidReward(fees: Long, reward: Long): Boolean {
        val senderAddress = consensusProperties.genesisAddress!!
        val bank = walletService.getBalanceByAddress(senderAddress)
        val rewardBlock = consensusProperties.rewardBlock!!

        return reward == fees + if (rewardBlock > bank) bank else rewardBlock
    }

    private fun isValidHash(timestamp: Long, fee: Long, senderAddress: String, payload: TransactionPayload, hash: String): Boolean =
        TransactionUtils.generateHash(timestamp, fee, senderAddress, payload) == hash

    private fun isValidSignature(hash: String, signature: String, publicKey: String): Boolean =
        SignatureUtils.verify(ByteUtils.fromHexString(hash), signature, ByteUtils.fromHexString(publicKey))

    private fun updateTransferBalance(to: String, amount: Long) {
        walletService.increaseBalance(to, amount)

        val senderAddress = consensusProperties.genesisAddress!!
        val bank = walletService.getBalanceByAddress(senderAddress)
        val reward = if (consensusProperties.rewardBlock!! > bank) bank else consensusProperties.rewardBlock!!

        walletService.decreaseBalance(consensusProperties.genesisAddress!!, reward)
    }

}