package io.openfuture.chain.core.model.entity.block.payload

import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.state.AccountState
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.PostLoad
import javax.persistence.Transient

@Embeddable
class MainBlockPayload(

    @Column(name = "transaction_merkle_hash", nullable = false)
    var transactionMerkleHash: String,

    @Column(name = "state_merkle_hash", nullable = false)
    var stateMerkleHash: String,

    @Column(name = "receipt_merkle_hash", nullable = false)
    var receiptMerkleHash: String,

    @Transient
    var rewardTransactions: List<RewardTransaction>,

    @Transient
    var voteTransactions: List<VoteTransaction>,

    @Transient
    var delegateTransactions: List<DelegateTransaction>,

    @Transient
    var transferTransactions: List<TransferTransaction>,

    @Transient
    var delegateStates: List<DelegateState>,

    @Transient
    var accountStates: List<AccountState>,

    @Transient
    var receipts: List<Receipt>

) : BlockPayload {

    @PostLoad
    private fun init() {
        rewardTransactions = listOf()
        delegateTransactions = listOf()
        voteTransactions = listOf()
        transferTransactions = listOf()
        delegateStates = listOf()
        accountStates = listOf()
        receipts = listOf()
    }

    override fun getBytes(): ByteArray =
        transactionMerkleHash.toByteArray() + stateMerkleHash.toByteArray() + receiptMerkleHash.toByteArray()

}