package io.openfuture.chain.core.model.entity

import io.openfuture.chain.core.model.entity.base.BaseModel
import javax.persistence.*

@Entity
@Table(name = "wallets")
class Wallet(

    @Column(name = "address", nullable = false)
    var address: String,

    @Column(name = "balance", nullable = false)
    var balance: Long = 0,

    @Column(name = "unconfirmed_out", nullable = false)
    var uncomfirmedOut: Long = 0,

    @Column(name = "unconfirmed_input", nullable = false)
    var unconfirmedInput: Long = 0,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "wallets2delegates",
        joinColumns = [(JoinColumn(name = "wallet_id", nullable = false))],
        inverseJoinColumns = [(JoinColumn(name = "delegate_id", nullable = false))]
    )
    val votes: MutableSet<Delegate> = mutableSetOf()

) : BaseModel()