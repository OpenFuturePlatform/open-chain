package io.openfuture.chain.core.model.entity

import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "wallet_votes")
class WalletVote(

    @EmbeddedId
    var id: WalletVoteId

) {

    constructor(address: String, nodeId: String) : this(WalletVoteId(address, nodeId))

}

@Embeddable
data class WalletVoteId(

    @Column(name = "address", nullable = false)
    var address: String,

    @Column(name = "node_id", nullable = false)
    var nodeId: String

) : Serializable