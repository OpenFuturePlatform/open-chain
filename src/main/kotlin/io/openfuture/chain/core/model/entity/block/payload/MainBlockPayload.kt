package io.openfuture.chain.core.model.entity.block.payload

import com.fasterxml.jackson.annotation.JsonIgnore
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.model.entity.state.WalletState
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import java.nio.charset.StandardCharsets.UTF_8
import javax.persistence.*

@Embeddable
class MainBlockPayload(

    @Column(name = "merkle_hash", nullable = false)
    var merkleHash: String,

    @Column(name = "state_hash", nullable = false)
    var stateHash: String,

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "transactions",
        joinColumns = [JoinColumn(name = "block_id")],
        inverseJoinColumns = [JoinColumn(name = "id")]
    )
    var rewardTransaction: MutableList<RewardTransaction> = mutableListOf(),

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "transactions",
        joinColumns = [JoinColumn(name = "block_id")],
        inverseJoinColumns = [JoinColumn(name = "id")]
    )
    var voteTransactions: MutableList<VoteTransaction> = mutableListOf(),

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "transactions",
        joinColumns = [JoinColumn(name = "block_id")],
        inverseJoinColumns = [JoinColumn(name = "id")]
    )
    var delegateTransactions: MutableList<DelegateTransaction> = mutableListOf(),

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "transactions",
        joinColumns = [JoinColumn(name = "block_id")],
        inverseJoinColumns = [JoinColumn(name = "id")]
    )
    var transferTransactions: MutableList<TransferTransaction> = mutableListOf(),

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "states",
        joinColumns = [JoinColumn(name = "block_id")],
        inverseJoinColumns = [JoinColumn(name = "id")]
    )
    var delegateStates: MutableList<DelegateState> = mutableListOf(),

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "states",
        joinColumns = [JoinColumn(name = "block_id")],
        inverseJoinColumns = [JoinColumn(name = "id")]
    )
    var walletStates: MutableList<WalletState> = mutableListOf()

) : BlockPayload {

    override fun getBytes(): ByteArray = merkleHash.toByteArray(UTF_8) + stateHash.toByteArray(UTF_8)

}