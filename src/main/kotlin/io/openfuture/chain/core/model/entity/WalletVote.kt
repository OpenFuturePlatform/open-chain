package io.openfuture.chain.core.model.entity

import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "wallet_votes")
class WalletVote(

    @EmbeddedId
    var id: WalletVoteId

) {

    constructor(address: String, delegateKey: String) : this(WalletVoteId(address, delegateKey))

}

@Embeddable
data class WalletVoteId(

    @Column(name = "address", nullable = false)
    var address: String,

    @Column(name = "delegate_key", nullable = false)
    var delegateKey: String

) : Serializable