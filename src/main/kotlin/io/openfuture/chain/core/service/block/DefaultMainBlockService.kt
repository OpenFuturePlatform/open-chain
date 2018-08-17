package io.openfuture.chain.core.service.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.model.entity.transaction.payload.RewardTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.repository.MainBlockRepository
import io.openfuture.chain.core.service.*
import io.openfuture.chain.core.util.BlockUtils
import io.openfuture.chain.core.util.TransactionUtils
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.MainBlockMessage
import io.openfuture.chain.network.message.core.RewardTransactionMessage
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultMainBlockService(
    blockService: BlockService,
    repository: MainBlockRepository,
    delegateService: DelegateService,
    private val clock: NodeClock,
    private val keyHolder: NodeKeyHolder,
    private val voteTransactionService: VoteTransactionService,
    private val delegateTransactionService: DelegateTransactionService,
    private val transferTransactionService: TransferTransactionService,
    private val rewardTransactionService: RewardTransactionService,
    private val consensusProperties: ConsensusProperties,
    private val walletService: WalletService
) : BaseBlockService<MainBlock>(repository, blockService, delegateService), MainBlockService {

    @Transactional(readOnly = true)
    override fun create(): PendingBlockMessage {
        val timestamp = clock.networkTime()
        val lastBlock = blockService.getLast()
        val height = lastBlock.height + 1
        val previousHash = lastBlock.hash

        // -- transactions by type
        val voteTransactions = voteTransactionService.getAllUnconfirmed()
        val delegateTransactions = delegateTransactionService.getAllUnconfirmed()
        val transferTransactions = transferTransactionService.getAllUnconfirmed()
        val transactions = voteTransactions + delegateTransactions + transferTransactions
        val rewardTransactionMessage = createRewardTransaction(timestamp, transactions)

        val transactionHashes = transactions.map { it.hash } + rewardTransactionMessage.hash

        val merkleHash = calculateMerkleRoot(transactionHashes)
        val payload = MainBlockPayload(merkleHash)

        val hash = BlockUtils.createHash(timestamp, height, previousHash, payload)
        val signature = SignatureUtils.sign(hash, keyHolder.getPrivateKey())
        val publicKey = keyHolder.getPublicKey()

        return PendingBlockMessage(height, previousHash, timestamp, ByteUtils.toHexString(hash), signature, publicKey,
            merkleHash, rewardTransactionMessage, voteTransactions.map { it.hash }, delegateTransactions.map { it.hash },
            transferTransactions.map { it.hash })
    }

    @Transactional
    override fun add(message: PendingBlockMessage) {
        if (null != repository.findOneByHash(message.hash)) {
            return
        }

        if (!isValid(message)) {
            //TODO call second synchronization
            return
        }

        val savedBlock = repository.save(MainBlock.of(message))
        rewardTransactionService.toBlock(message.rewardTransaction, savedBlock)
        message.voteTransactions.forEach { voteTransactionService.toBlock(it, savedBlock) }
        message.delegateTransactions.forEach { delegateTransactionService.toBlock(it, savedBlock) }
        message.transferTransactions.forEach { transferTransactionService.toBlock(it, savedBlock) }
    }

    @Transactional
    override fun synchronize(message: MainBlockMessage) {
        if (null != repository.findOneByHash(message.hash)) {
            return
        }

        val block = MainBlock.of(message)
        val transactionHashes = message.getAllTransactions().map { it.hash } + message.rewardTransaction.hash
        if (!isValid(block, transactionHashes) || !rewardTransactionService.verify(message)) {
            return
        }

        val savedBlock = repository.save(block)
        rewardTransactionService.toBlock(message.rewardTransaction, savedBlock)
        message.voteTransactions.forEach { voteTransactionService.synchronize(it, savedBlock) }
        message.delegateTransactions.forEach { delegateTransactionService.synchronize(it, savedBlock) }
        message.transferTransactions.forEach { transferTransactionService.synchronize(it, savedBlock) }
    }

    @Transactional(readOnly = true)
    override fun isValid(message: PendingBlockMessage): Boolean {
        val transactionHashes = message.getAllTransactions() + message.rewardTransaction.hash
        return isValid(MainBlock.of(message), transactionHashes) && rewardTransactionService.verify(message)
    }

    private fun isValid(block: MainBlock, transactions: List<String>): Boolean {
        return isValidMerkleHash(block.payload.merkleHash, transactions) && super.isValid(block)
    }

    private fun calculateMerkleRoot(transactions: List<String>): String {
        if (transactions.size == 1) {
            return transactions.single()
        }
        var previousTreeLayout = transactions.sortedByDescending { it }.map { it.toByteArray() }
        var treeLayout = mutableListOf<ByteArray>()
        while (previousTreeLayout.size != 2) {
            for (i in 0 until previousTreeLayout.size step 2) {
                val leftHash = previousTreeLayout[i]
                val rightHash = if (i + 1 == previousTreeLayout.size) {
                    previousTreeLayout[i]
                } else {
                    previousTreeLayout[i + 1]
                }
                treeLayout.add(HashUtils.sha256(leftHash + rightHash))
            }
            previousTreeLayout = treeLayout
            treeLayout = mutableListOf()
        }
        return ByteUtils.toHexString(HashUtils.doubleSha256(previousTreeLayout[0] + previousTreeLayout[1]))
    }

    private fun isValidMerkleHash(merkleHash: String, transactions: List<String>): Boolean =
        merkleHash == calculateMerkleRoot(transactions)

    private fun createRewardTransaction(timestamp: Long, transactions: List<UnconfirmedTransaction>): RewardTransactionMessage {
        val senderAddress = consensusProperties.genesisAddress!!
        val rewardBlock = consensusProperties.rewardBlock!!
        val bank = walletService.getBalanceByAddress(senderAddress)
        val reward = transactions.map { it.fee }.sum() + if (rewardBlock > bank) bank else rewardBlock
        val fee = 0L
        val publicKey = keyHolder.getPublicKey()
        val delegate = delegateService.getByPublicKey(publicKey)

        val payload = RewardTransactionPayload(reward, delegate.address)
        val hash = TransactionUtils.generateHash(timestamp, fee, senderAddress, payload)
        val signature = SignatureUtils.sign(ByteUtils.fromHexString(hash), keyHolder.getPrivateKey())

        return RewardTransactionMessage(timestamp, fee, senderAddress, hash, signature, publicKey, reward, delegate.address)
    }

}