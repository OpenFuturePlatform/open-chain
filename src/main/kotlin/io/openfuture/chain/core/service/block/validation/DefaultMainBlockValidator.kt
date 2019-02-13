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
import io.openfuture.chain.core.service.MainBlockValidator
import io.openfuture.chain.core.service.ReceiptService
import io.openfuture.chain.core.service.StateManager
import io.openfuture.chain.core.service.TransactionManager
import io.openfuture.chain.crypto.util.HashUtils
import org.springframework.stereotype.Service

@Service
class DefaultMainBlockValidator(
    private val consensusProperties: ConsensusProperties,
    private val stateManager: StateManager,
    private val transactionManager: TransactionManager,
    private val receiptService: ReceiptService,
    private val statePool: StatePool
) : MainBlockValidator {

    override fun validate(block: MainBlock, new: Boolean) {
        checkStateMerkleHash(block)
        checkTransactionMerkleHash(block)
        checkReceiptMerkleHash(block)
        if (new) {
            checkBalances(block)
        }
        checkRewardTransaction(block, new)
        checkDelegateTransactions(block)
        checkTransferTransactions(block)
        checkVoteTransactions(block, new)
        checkReceiptsAndStates(block, new)
    }

    private fun checkBalances(block: MainBlock) {
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

    private fun checkStateMerkleHash(block: MainBlock) {
        val states = block.getPayload().delegateStates + block.getPayload().accountStates

        if (!verifyMerkleRootHash(block.getPayload().stateMerkleHash, states.map { it.hash })) {
            throw ValidationException("Invalid state merkle hash in block: height #${block.height}, hash ${block.hash}")
        }
    }

    private fun checkTransactionMerkleHash(block: MainBlock) {
        val transactions = block.getPayload().delegateTransactions + block.getPayload().transferTransactions +
            block.getPayload().voteTransactions + block.getPayload().rewardTransactions

        if (!verifyMerkleRootHash(block.getPayload().transactionMerkleHash, transactions.map { it.hash })) {
            throw ValidationException("Invalid transaction merkle hash in block: height #${block.height}, hash ${block.hash}")
        }
    }

    private fun checkReceiptMerkleHash(block: MainBlock) {
        if (!verifyMerkleRootHash(block.getPayload().receiptMerkleHash, block.getPayload().receipts.map { it.hash })) {
            throw ValidationException("Invalid receipt merkle hash in block: height #${block.height}, hash ${block.hash}")
        }
    }

    private fun checkReceiptsAndStates(block: MainBlock, new: Boolean) {
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
            val delegateWallet = stateManager.getLastByAddress<DelegateState>(block.publicKey).walletAddress
            val transactions = block.getPayload().delegateTransactions + block.getPayload().transferTransactions +
                block.getPayload().voteTransactions + block.getPayload().rewardTransactions
            val receipts = transactionManager.processTransactions(transactions, delegateWallet)
            val states = statePool.getStates()

            if (block.getPayload().receipts.size != receipts.size) {
                throw ValidationException("Invalid count block receiptsin block: height #${block.height}, hash ${block.hash}")
            }

            if (blockStates.size != states.size) {
                throw ValidationException("Invalid count block statesin block: height #${block.height}, hash ${block.hash}")
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

    private fun checkRewardTransaction(block: MainBlock, new: Boolean) {
        val rewardTransaction = block.getPayload().rewardTransactions.firstOrNull()
            ?: throw ValidationException("Missing reward transaction in block: height #${block.height}, hash ${block.hash}")

        if (new) {
            val externalTransactions = block.getPayload().delegateTransactions + block.getPayload().transferTransactions +
                block.getPayload().voteTransactions
            val fees = externalTransactions.asSequence().map { it.fee }.sum()

            if (!verifyReward(fees, rewardTransaction.getPayload().reward)) {
                throw ValidationException("Invalid fee of reward transaction in block: height #${block.height}, hash ${block.hash}")
            }
        }

        if (!transactionManager.verify(rewardTransaction)) {
            throw ValidationException("Invalid reward transaction in block: height #${block.height}, hash ${block.hash}")
        }
    }

    private fun checkDelegateTransactions(block: MainBlock) {
        val transactions = block.getPayload().delegateTransactions

        if (transactions.distinctBy { it.getPayload().delegateKey }.size != transactions.size) {
            throw ValidationException("Invalid delegate transactions in block: height #${block.height}, hash ${block.hash}")
        }

        transactions.forEach {
            if (!transactionManager.verify(it)) {
                throw ValidationException("Invalid delegate transactions in block: height #${block.height}, hash ${block.hash}")
            }
        }
    }

    private fun checkTransferTransactions(block: MainBlock) {
        block.getPayload().transferTransactions.forEach {
            if (!transactionManager.verify(it)) {
                throw ValidationException("Invalid transfer transactions in block: height #${block.height}, hash ${block.hash}")
            }
        }
    }

    private fun checkVoteTransactions(block: MainBlock, new: Boolean) {
        val transactions = block.getPayload().voteTransactions

        transactions.groupBy { it.senderAddress }.entries.forEach {
            if (it.value.size != 1) {
                throw ValidationException("Invalid vote transactions in block: height #${block.height}, hash ${block.hash}")
            }

            if (new) {
                val accountState = stateManager.getLastByAddress<AccountState>(it.key)
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

        transactions.forEach {
            if (!transactionManager.verify(it)) {
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

    private fun verifyReward(fees: Long, reward: Long): Boolean {
        val senderAddress = consensusProperties.genesisAddress!!
        val bank = stateManager.getWalletBalanceByAddress(senderAddress)
        val rewardBlock = consensusProperties.rewardBlock!!

        return reward == (fees + if (rewardBlock > bank) bank else rewardBlock)
    }

}