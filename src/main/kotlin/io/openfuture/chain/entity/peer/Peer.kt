package io.openfuture.chain.entity.peer

import io.openfuture.chain.domain.node.PeerDto
import io.openfuture.chain.entity.base.BaseModel
import javax.persistence.*

@Entity
@Table(name = "peers")
@Inheritance(strategy = InheritanceType.JOINED)
open class Peer(

    @Column(name = "network_id", nullable = false)
    var networkId: String,

    @Column(name = "host", nullable = false)
    var host: String,

    @Column(name = "port", nullable = false)
    var port: Int

) : BaseModel() {

    constructor(dto: PeerDto) : this(
        dto.networkId,
        dto.host,
        dto.port
    )

}
