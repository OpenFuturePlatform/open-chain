package io.openfuture.chain.entity

import io.openfuture.chain.domain.delegate.DelegateDto
import io.openfuture.chain.entity.base.BaseModel
import javax.persistence.*

@Embeddable

@Entity
@Table(name = "delegates")
class Delegate(

    @Column(name = "public_key", nullable = false)
    var publicKey: String,

    @Column(name = "host", nullable = false)
    var host: String,

    @Column(name = "port", nullable = false)
    var port: Int,

    @Column(name = "rating", nullable = false)
    var rating: Double = 0.0

) : BaseModel() {

    companion object {
        fun of(dto: DelegateDto): Delegate = Delegate(
            dto.publicKey,
            dto.networkAddress.host,
            dto.networkAddress.port
        )
    }

}