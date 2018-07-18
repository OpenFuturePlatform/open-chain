package io.openfuture.chain.entity.memory

import io.openfuture.chain.entity.Delegate
import io.openfuture.chain.entity.Wallet
import io.openfuture.chain.entity.base.BaseModel
import io.openfuture.chain.entity.transaction.VoteTransaction
import javax.persistence.*

@Entity
@Table(name = "mem_votes")
class MemVote(

    @ManyToOne
    @JoinColumn(name = "transaction_id", nullable = false)
    private var tx: VoteTransaction,

    @Column(name = "type_id", nullable = false)
    private var typeId: Int,

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    val wallet: Wallet,

    @ManyToOne
    @JoinColumn(name = "delegate_id", nullable = false)
    val delegate: Delegate

) : BaseModel()