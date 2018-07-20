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
            delegateDto.info.networkAddress.host,
            delegateDto.info.networkAddress.port,
            delegateDto.rating
        )
    }

    fun getAddress(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}