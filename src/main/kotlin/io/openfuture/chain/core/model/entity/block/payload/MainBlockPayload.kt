package io.openfuture.chain.core.model.entity.block.payload

import com.fasterxml.jackson.annotation.JsonIgnore
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.state.AccountState
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import javax.persistence.*

@Embeddable
class MainBlockPayload(

    @Column(name = "transaction_merkle_hash", nullable = false)
    var transactionMerkleHash: String,

    @Column(name = "state_merkle_hash", nullable = false)
    var stateMerkleHash: String,

    @Column(name = "receipt_merkle_hash", nullable = false)
    var receiptMerkleHash: String,

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "transactions",
        joinColumns = [JoinColumn(name = "block_id")],
        inverseJoinColumns = [JoinColumn(name = "id")]
    )
    @Fetch(value = FetchMode.SUBSELECT)
    var rewardTransaction: MutableList<RewardTransaction> = mutableListOf(),

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "transactions",
        joinColumns = [JoinColumn(name = "block_id")],
        inverseJoinColumns = [JoinColumn(name = "id")]
    )
    @Fetch(value = FetchMode.SUBSELECT)
    var voteTransactions: MutableList<VoteTransaction> = mutableListOf(),

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "transactions",
        joinColumns = [JoinColumn(name = "block_id")],
        inverseJoinColumns = [JoinColumn(name = "id")]
    )
    @Fetch(value = FetchMode.SUBSELECT)
    var delegateTransactions: MutableList<DelegateTransaction> = mutableListOf(),

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "transactions",
        joinColumns = [JoinColumn(name = "block_id")],
        inverseJoinColumns = [JoinColumn(name = "id")]
    )
    @Fetch(value = FetchMode.SUBSELECT)
    var transferTransactions: MutableList<TransferTransaction> = mutableListOf(),

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "states",
        joinColumns = [JoinColumn(name = "block_id")],
        inverseJoinColumns = [JoinColumn(name = "id")]
    )
    @Fetch(value = FetchMode.SUBSELECT)
    var delegateStates: MutableList<DelegateState> = mutableListOf(),

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "states",
        joinColumns = [JoinColumn(name = "block_id")],
        inverseJoinColumns = [JoinColumn(name = "id")]
    )
    @Fetch(value = FetchMode.SUBSELECT)
    var accountStates: MutableList<AccountState> = mutableListOf(),

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "receipts",
        joinColumns = [JoinColumn(name = "block_id")],
        inverseJoinColumns = [JoinColumn(name = "id")]
    )
    @Fetch(value = FetchMode.SUBSELECT)
    var receipts: MutableList<Receipt> = mutableListOf()

) : BlockPayload {

    override fun getBytes(): ByteArray =
        transactionMerkleHash.toByteArray() + stateMerkleHash.toByteArray() + receiptMerkleHash.toByteArray()

}