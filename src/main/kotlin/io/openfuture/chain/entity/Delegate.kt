package io.openfuture.chain.entity

import io.openfuture.chain.domain.delegate.DelegateDto
import io.openfuture.chain.entity.base.BaseModel
import javax.persistence.*

@Entity
@Table(name = "delegates")
class Delegate(

    @Column(name = "host", nullable = false)
    var host: String,

    @Column(name = "port", nullable = false)
    var port: Int,

    @Column(name = "rating", nullable = false)
    var rating: Int = 0

) : BaseModel() {

    companion object {
        fun of(delegateDto: DelegateDto): Delegate = Delegate(
            delegateDto.networkInfo.host,
            delegateDto.networkInfo.port,
            delegateDto.rating
        )
    }

}