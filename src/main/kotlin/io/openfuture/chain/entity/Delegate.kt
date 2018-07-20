package io.openfuture.chain.entity

import io.openfuture.chain.domain.delegate.DelegateDto
import io.openfuture.chain.entity.base.BaseModel
import javax.persistence.*

@Embeddable

@Entity
@Table(name = "delegates")
class Delegate(

    @Column(name = "public_key", nullable = false, unique = true)
    var publicKey: String,

    @Column(name = "host", nullable = false)
    var host: String,

    @Column(name = "port", nullable = false)
    var port: Int,

    id: Int = 0

) : BaseModel(id) {

    companion object {
        fun of(dto: DelegateDto): Delegate = Delegate(
            dto.key,
            dto.networkAddress.host,
            dto.networkAddress.port
        )
    }

}