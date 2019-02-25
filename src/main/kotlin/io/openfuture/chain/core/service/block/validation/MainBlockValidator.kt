package io.openfuture.chain.core.service.block.validation

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.component.StatePool
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.dictionary.VoteType.AGAINST
import io.openfuture.chain.core.model.entity.dictionary.VoteType.FOR
import io.openfuture.chain.core.model.entity.state.AccountState
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.service.ReceiptService
import io.openfuture.chain.core.service.StateManager
import io.openfuture.chain.core.service.TransactionManager
import io.openfuture.chain.core.service.transaction.validation.DelegateTransactionValidator
import io.openfuture.chain.core.service.transaction.validation.RewardTransactionValidator
import io.openfuture.chain.core.service.transaction.validation.TransferTransactionValidator
import io.openfuture.chain.core.service.transaction.validation.VoteTransactionValidator
import io.openfuture.chain.core.service.transaction.validation.pipeline.TransactionValidationPipeline
import io.openfuture.chain.core.util.BlockValidateHandler
import io.openfuture.chain.crypto.util.HashUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MainBlockValidator(
    private val consensusProperties: ConsensusProperties,
    private val stateManager: StateManager,
    private val transactionManager: TransactionManager,
    private val rewardTransactionValidator: RewardTransactionValidator,
    private val delegateTransactionValidator: DelegateTransactionValidator,
    private val transferTransactionValidator: TransferTransactionValidator,
    private val voteTransactionValidator: VoteTransactionValidator,
    private val receiptService: ReceiptService,
    private val statePool: StatePool
) : BlockValidator() {

    fun checkLight(): Array<BlockValidateHandler> = arrayOf(
        *checkLightOnSync(),
        checkStateMerkleHash()
    )

    fun checkLightOnSync(): Array<BlockValidateHandler> = arrayOf(
        checkSignature(),
        checkHash(),
        checkTimeStamp(),
        checkHeight(),
        checkPreviousHash()
    )

    fun checkFull(): Array<BlockValidateHandler> = arrayOf(
        *checkLight(),
        checkTransactionMerkleHash(),
        checkReceiptMerkleHash(),
        checkBalances(),
        checkRewardTransaction(),
        checkDelegateTransactions(),
        checkTransferTransactions(),
        checkVoteTransactions(),
        checkReceiptsAndStates()
    )

    fun checkFullOnSync(): Array<BlockValidateHandler> = arrayOf(
        *checkLightOnSync(),
        checkTransactionMerkleHash(),
        checkReceiptMerkleHash(),
        checkTransactionsHashes()
    )

    fun checkBalances(): BlockValidateHandler = { block, _, _, new ->
        block as MainBlock
        if (new) {
            val transactions = block.getPayload().delegateTransactions + block.getPayload().transferTransactions +
                block.getPayload().voteTransactions

            val result = transactions.groupBy { it.senderAddress }.entries.all { sender ->
                sender.value.asSequence().map {
                    when (it) {
                        is TransferTransaction -> it.getPayload().amount + it.fee
                        is DelegateTransaction -> it.getPayload().amount + it.fee
                        is VoteTransaction -> it.fee
                        else -> 0
                    }
                }.sum() <= stateManager.getWalletBalanceByAddress(sender.key)
            }

            if (!result) {
                throw ValidationException("Invalid balances in block: height #${block.height}, hash ${block.hash}")
            }
        }
    }

    fun checkStateMerkleHash(): BlockValidateHandler = { block, _, lastMainBlock, _ ->
        block as MainBlock
        val states = block.getPayload().delegateStates + block.getPayload().accountStates
        if (!states.isEmpty()) {
            val stateHashes = (states.map { it.hash }) as MutableList
            stateHashes.add(lastMainBlock.getPayload().stateMerkleHash)

            if (!verifyMerkleRootHash(block.getPayload().stateMerkleHash, stateHashes)) {
                throw ValidationException("Invalid state merkle hash in block: height #${block.height}, hash ${block.hash}")
            }
        }
    }

    fun checkTransactionMerkleHash(): BlockValidateHandler = { block, _, _, _ ->
        block as MainBlock
        val transactions = block.getPayload().delegateTransactions + block.getPayload().transferTransactions +
            block.getPayload().voteTransactions + block.getPayload().rewardTransactions

        if (!verifyMerkleRootHash(block.getPayload().transactionMerkleHash, transactions.map { it.hash })) {
            throw ValidationException("Invalid transaction merkle hash in block: height #${block.height}, hash ${block.hash}")
        }
    }

    fun checkReceiptMerkleHash(): BlockValidateHandler = { block, _, _, _ ->
        block as MainBlock
        if (!verifyMerkleRootHash(block.getPayload().receiptMerkleHash, block.getPayload().receipts.map { it.hash })) {
            throw ValidationException("Invalid receipt merkle hash in block: height #${block.height}, hash ${block.hash}")
        }
    }

    fun checkReceiptsAndStates(): BlockValidateHandler = { block, _, _, new ->
        block as MainBlock
        val blockStates = block.getPayload().delegateStates + block.getPayload().accountStates

        blockStates.forEach {
            if (!stateManager.verify(it)) {
                throw ValidationException("Invalid block states in block: height #${block.height}, hash ${block.hash}")
            }
        }

        block.getPayload().receipts.forEach {
            if (!receiptService.verify(it)) {
                throw ValidationException("Invalid block receipts in block: height #${block.height}, hash ${block.hash}")
            }
        }

        if (new) {
            val delegateWallet = stateManager.getByAddress<DelegateState>(block.publicKey).walletAddress
            val transactions = block.getPayload().delegateTransactions + block.getPayload().transferTransactions +
                block.getPayload().voteTransactions + block.getPayload().rewardTransactions
            val receipts = transactionManager.processTransactions(transactions, delegateWallet)
            val states = statePool.getStates()

            if (block.getPayload().receipts.size != receipts.size) {
                throw ValidationException("Invalid count block receipts in block: height #${block.height}, hash ${block.hash}")
            }

            if (blockStates.size != states.size) {
                throw ValidationException("Invalid count block states in block: height #${block.height}, hash ${block.hash}")
            }

            receipts.forEach { r ->
                block.getPayload().receipts.firstOrNull { it.hash == r.hash }
                    ?: throw ValidationException("Invalid block receipts in block: height #${block.height}, hash ${block.hash}")
            }

            states.forEach { s ->
                blockStates.firstOrNull { it.hash == s.hash }
                    ?: throw ValidationException("Invalid block states in block: height #${block.height}, hash ${block.hash}")
            }
        }
    }

    fun checkTransactionsHashes(): BlockValidateHandler = { block, _, _, _ ->
        block as MainBlock
        block.getPayload().delegateTransactions.forEach { delegateTransactionValidator.checkHash().invoke(it) }
        block.getPayload().transferTransactions.forEach { transferTransactionValidator.checkHash().invoke(it) }
        block.getPayload().voteTransactions.forEach { voteTransactionValidator.checkHash().invoke(it) }
        block.getPayload().rewardTransactions.forEach { rewardTransactionValidator.checkHash().invoke(it) }
    }

    fun checkRewardTransaction(): BlockValidateHandler = { block, _, _, new ->
        block as MainBlock
        val rewardTransaction = block.getPayload().rewardTransactions.firstOrNull()
            ?: throw ValidationException("Missing reward transaction in block: height #${block.height}, hash ${block.hash}")

        if (new) {
            if (!verifyReward(rewardTransaction.getPayload().reward)) {
                throw ValidationException("Invalid fee of reward transaction in block: height #${block.height}, hash ${block.hash}")
            }
        }

        val pipeline = TransactionValidationPipeline(rewardTransactionValidator.check())
        if (!rewardTransactionValidator.verify(rewardTransaction, pipeline)) {
            throw ValidationException("Invalid reward transaction in block: height #${block.height}, hash ${block.hash}")
        }
    }

    fun checkDelegateTransactions(): BlockValidateHandler = { block, _, _, _ ->
        block as MainBlock
        val transactions = block.getPayload().delegateTransactions

        if (transactions.distinctBy { it.getPayload().delegateKey }.size != transactions.size) {
            throw ValidationException("Invalid delegate transactions in block: height #${block.height}, hash ${block.hash}")
        }

        val pipeline = TransactionValidationPipeline(delegateTransactionValidator.check())
        transactions.forEach {
            if (!delegateTransactionValidator.verify(it, pipeline)) {
                throw ValidationException("Invalid delegate transactions in block: height #${block.height}, hash ${block.hash}")
            }
        }
    }

    fun checkTransferTransactions(): BlockValidateHandler = { block, _, _, _ ->
        block as MainBlock

        val pipeline = TransactionValidationPipeline(transferTransactionValidator.check())
        block.getPayload().transferTransactions.forEach {
            if (!transferTransactionValidator.verify(it, pipeline)) {
                throw ValidationException("Invalid transfer transactions in block: height #${block.height}, hash ${block.hash}")
            }
        }
    }

    fun checkVoteTransactions(): BlockValidateHandler = { block, _, _, new ->
        block as MainBlock
        val transactions = block.getPayload().voteTransactions

        transactions.groupBy { it.senderAddress }.entries.forEach {
            if (it.value.size != 1) {
                throw ValidationException("Invalid vote transactions in block: height #${block.height}, hash ${block.hash}")
            }

            if (new) {
                val accountState = stateManager.getByAddress<AccountState>(it.key)
                val vote = it.value.first()

                val result = when (vote.getPayload().getVoteType()) {
                    FOR -> null == accountState.voteFor
                    AGAINST -> null != accountState.voteFor && vote.getPayload().delegateKey == accountState.voteFor
                }

                if (!result) {
                    throw ValidationException("Invalid vote transactions in block: height #${block.height}, hash ${block.hash}")
                }
            }
        }

        val pipeline = TransactionValidationPipeline(voteTransactionValidator.check())
        transactions.forEach {
            if (!voteTransactionValidator.verify(it, pipeline)) {
                throw ValidationException("Invalid vote transactions in block: height #${block.height}, hash ${block.hash}")
            }
        }
    }

    private fun verifyMerkleRootHash(rootHash: String, hashes: List<String>): Boolean {
        if (hashes.isEmpty()) {
            return false
        }

        return rootHash == HashUtils.merkleRoot(hashes)
    }

    private fun verifyReward(reward: Long): Boolean {
        val senderAddress = consensusProperties.genesisAddress!!
        val bank = stateManager.getWalletBalanceByAddress(senderAddress)
        val rewardBlock = consensusProperties.rewardBlock!!

        return reward == if (rewardBlock > bank) bank else rewardBlock
    }

}