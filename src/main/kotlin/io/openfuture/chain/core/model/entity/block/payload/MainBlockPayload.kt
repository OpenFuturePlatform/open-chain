package io.openfuture.chain.core.model.entity.block.payload

import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import java.nio.charset.StandardCharsets.UTF_8
import javax.persistence.*

@Embeddable
class MainBlockPayload(

    @Column(name = "merkle_hash", nullable = false)
    var merkleHash: String,

    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "transactions",
        joinColumns = [JoinColumn(name = "block_id")],
        inverseJoinColumns = [JoinColumn(name = "id")]
    )
    var voteTransactions: MutableList<VoteTransaction> = mutableListOf(),

    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "transactions",
        joinColumns = [JoinColumn(name = "block_id")],
        inverseJoinColumns = [JoinColumn(name = "id")]
    )
    var delegateTransactions: MutableList<DelegateTransaction> = mutableListOf(),

    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "transactions",
        joinColumns = [JoinColumn(name = "block_id")],
        inverseJoinColumns = [JoinColumn(name = "id")]
    )
    var transferTransactions: MutableList<TransferTransaction> = mutableListOf()


) : BlockPayload {

    override fun getBytes(): ByteArray = merkleHash.toByteArray(UTF_8)

}