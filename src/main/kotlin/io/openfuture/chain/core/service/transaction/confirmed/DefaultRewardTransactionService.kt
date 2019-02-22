package io.openfuture.chain.core.service.transaction.confirmed

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.ReceiptResult
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.RewardTransactionPayload
import io.openfuture.chain.core.repository.RewardTransactionRepository
import io.openfuture.chain.core.service.RewardTransactionService
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultRewardTransactionService(
    private val repository: RewardTransactionRepository,
    private val consensusProperties: ConsensusProperties,
    private val keyHolder: NodeKeyHolder
) : DefaultTransactionService<RewardTransaction>(repository), RewardTransactionService {

    override fun getByBlock(block: Block): RewardTransaction? = repository.findAllByBlock(block).firstOrNull()

    override fun getByRecipientAddress(address: String): List<RewardTransaction> =
        repository.findAllByPayloadRecipientAddress(address)

    override fun create(timestamp: Long): RewardTransaction {
        val senderAddress = consensusProperties.genesisAddress!!
        val rewardBlock = consensusProperties.rewardBlock!!
        val bank = stateManager.getWalletBalanceByAddress(senderAddress)
        val reward = if (rewardBlock > bank) bank else rewardBlock
        val fee = 0L
        val publicKey = keyHolder.getPublicKeyAsHexString()
        val delegate = stateManager.getByAddress<DelegateState>(publicKey)
        val hash = RewardTransaction.generateHash(timestamp, fee, senderAddress, reward, delegate.walletAddress)
        val signature = SignatureUtils.sign(ByteUtils.fromHexString(hash), keyHolder.getPrivateKey())

        return RewardTransaction(timestamp, fee, senderAddress, hash, signature, publicKey,
            RewardTransactionPayload(reward, delegate.walletAddress))
    }

    @Transactional
    override fun commit(tx: RewardTransaction, receipt: Receipt): RewardTransaction {
        BlockchainLock.writeLock.lock()
        try {
            val persistedTx = repository.findOneByHash(tx.hash)
            if (null != persistedTx) {
                return persistedTx
            }

            return repository.save(tx)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    override fun process(tx: RewardTransaction, delegateWallet: String): Receipt {
        stateManager.updateWalletBalanceByAddress(tx.getPayload().recipientAddress, tx.getPayload().reward)

        val senderAddress = consensusProperties.genesisAddress!!
        val bank = stateManager.getWalletBalanceByAddress(senderAddress)
        val reward = if (consensusProperties.rewardBlock!! > bank) bank else consensusProperties.rewardBlock!!

        stateManager.updateWalletBalanceByAddress(senderAddress, -reward)

        return generateReceipt(tx)
    }

    private fun generateReceipt(tx: RewardTransaction): Receipt {
        val results = listOf(ReceiptResult(tx.senderAddress, tx.getPayload().recipientAddress, tx.getPayload().reward))

        return getReceipt(tx.hash, results)
    }

}