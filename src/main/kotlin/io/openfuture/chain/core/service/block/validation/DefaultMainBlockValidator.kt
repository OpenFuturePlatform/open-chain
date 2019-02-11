package io.openfuture.chain.core.service.block.validation

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.service.MainBlockValidator
import org.springframework.stereotype.Service

@Service
class DefaultMainBlockValidator : MainBlockValidator {

    override fun validate(block: MainBlock) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


//    private fun validate(message: PendingBlockMessage) {
//        if (!isValidRootHash(message.transactionMerkleHash, message.getAllTransactions().map { it.hash })) {
//            throw ValidationException("Invalid transaction merkle hash in block: height #${message.height}, hash ${message.hash}")
//        }
//
//        if (!isValidRootHash(message.stateMerkleHash, message.getAllStates().map { it.hash })) {
//            throw ValidationException("Invalid state merkle hash in block: height #${message.height}, hash ${message.hash}")
//        }
//
//        if (!isValidRootHash(message.receiptMerkleHash, message.receipts.map { it.hash })) {
//            throw ValidationException("Invalid receipt merkle hash in block: height #${message.height}, hash ${message.hash}")
//        }
//
//        if (!isValidBalances(message.getExternalTransactions())) {
//            throw ValidationException("Invalid balances in block: height #${message.height}, hash ${message.hash}")
//        }
//
//        if (!isValidRewardTransaction(message)) {
//            throw ValidationException("Invalid reward transaction in block: height #${message.height}, hash ${message.hash}")
//        }
//
//        if (!isValidVoteTransactions(message.voteTransactions)) {
//            throw ValidationException("Invalid vote transactions in block: height #${message.height}, hash ${message.hash}")
//        }
//
//        if (!isValidDelegateTransactions(message.delegateTransactions)) {
//            throw ValidationException("Invalid delegate transactions in block: height #${message.height}, hash ${message.hash}")
//        }
//
//        if (!isValidTransferTransactions(message.transferTransactions)) {
//            throw ValidationException("Invalid transfer transactions in block: height #${message.height}, hash ${message.hash}")
//        }
//
//        if (!isValidReceiptsAndStates(message)) {
//            throw ValidationException("Invalid block states and receipts")
//        }
//
//    }
//
//    private fun isValidBalances(transactions: List<TransactionMessage>): Boolean =
//        transactions.groupBy { it.senderAddress }.entries.all { sender ->
//            sender.value.asSequence()
//                .map {
//                    when (it) {
//                        is TransferTransactionMessage -> it.amount + it.fee
//                        is DelegateTransactionMessage -> it.amount + it.fee
//                        is VoteTransactionMessage -> it.fee
//                        else -> 0
//                    }
//                }
//                .sum() <= stateManager.getWalletBalanceByAddress(sender.key)
//        }
//
//    private fun isValidRewardTransaction(message: PendingBlockMessage): Boolean =
//        isValidReward(message.getExternalTransactions().asSequence().map { it.fee }.sum(), message.rewardTransaction.reward) &&
//            transactionManager.verify(RewardTransaction.of(message.rewardTransaction))
//
//    private fun isValidVoteTransactions(transactions: List<VoteTransactionMessage>): Boolean {
//        if (transactions.isEmpty()) {
//            return true
//        }
//
//        val validVotes = transactions.groupBy { it.senderAddress }.entries.all { sender ->
//            if (sender.value.size != 1) {
//                return false
//            }
//
//            val accountState = stateManager.getLastByAddress<AccountState>(sender.key)
//            val vote = sender.value.first()
//            if (VoteType.values().none { it.getId() == vote.voteTypeId }) {
//                throw ValidationException("Vote type with id: ${vote.voteTypeId} is not exists")
//            }
//
//            return when (VoteType.values().first { it.getId() == vote.voteTypeId }) {
//                VoteType.FOR -> null == accountState.voteFor
//                VoteType.AGAINST -> null != accountState.voteFor && vote.delegateKey == accountState.voteFor
//            }
//        }
//
//        return validVotes && transactions.all { transactionManager.verify(VoteTransaction.of(it)) }
//    }
//
//    private fun isValidDelegateTransactions(transactions: List<DelegateTransactionMessage>): Boolean {
//        if (transactions.isEmpty()) {
//            return true
//        }
//
//        return transactions.all { transactionManager.verify(DelegateTransaction.of(it)) } &&
//            !stateManager.isExistsDelegatesByPublicKeys(transactions.map { it.delegateKey }) &&
//            transactions.distinctBy { it.delegateKey }.size == transactions.size
//    }
//
//    private fun isValidTransferTransactions(transactions: List<TransferTransactionMessage>): Boolean =
//        transactions.all { transactionManager.verify(TransferTransaction.of(it)) }
//
//    private fun isValidRootHash(rootHash: String, hashes: List<String>): Boolean {
//        if (hashes.isEmpty()) {
//            return false
//        }
//
//        return rootHash == HashUtils.calculateMerkleRoot(hashes)
//    }
//
//    private fun isValidReward(fees: Long, reward: Long): Boolean {
//        val senderAddress = consensusProperties.genesisAddress!!
//        val bank = stateManager.getWalletBalanceByAddress(senderAddress)
//        val rewardBlock = consensusProperties.rewardBlock!!
//
//        return reward == (fees + if (rewardBlock > bank) bank else rewardBlock)
//    }
//
//    private fun isValidReceiptsAndStates(block: PendingBlockMessage): Boolean {
//        val delegateWallet = stateManager.getLastByAddress<DelegateState>(block.publicKey).walletAddress
//        val receipts = processTransactions(block.getAllTransactions(), delegateWallet).map { it.toMessage() }
//        val states = statePool.getStates()
//
//        if (block.receipts.size != receipts.size) {
//            return false
//        }
//
//        if (block.getAllStates().size != states.size) {
//            return false
//        }
//
//        return receipts.all { block.receipts.contains(it) } && states.all { block.getAllStates().contains(it.toMessage()) }
//    }
//
//    private fun processTransactions(txMessages: List<TransactionMessage>, delegateWallet: String): List<Receipt> {
//        val transactions = txMessages.map {
//            when (it) {
//                is TransferTransactionMessage -> TransferTransaction.of(it)
//                is VoteTransactionMessage -> VoteTransaction.of(it)
//                is DelegateTransactionMessage -> DelegateTransaction.of(it)
//                is RewardTransactionMessage -> RewardTransaction.of(it)
//                else -> throw IllegalStateException("Unsupported transaction type")
//            }
//        }
//
//        return transactionManager.processTransactions(transactions, delegateWallet)
//    }
}