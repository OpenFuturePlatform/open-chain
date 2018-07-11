package io.openfuture.chain.entity.peer

import io.openfuture.chain.domain.node.DelegateDto
import javax.persistence.*

@Entity
@Table(name = "delegates")
class Delegate(
    networkId: String,
    host: String,
    port: Int,

    @Column(name = "rating", nullable = false)
    var rating: Int = 0

) : Peer(networkId, host, port) {

    companion object {
        fun of(delegateDto: DelegateDto): Delegate = Delegate(
            delegateDto.networkId,
            delegateDto.host,
            delegateDto.port,
            delegateDto.rating
        )
    }

}