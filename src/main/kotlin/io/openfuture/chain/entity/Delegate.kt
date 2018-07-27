package io.openfuture.chain.entity

import io.openfuture.chain.entity.base.BaseModel
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table
import io.openfuture.chain.network.domain.NetworkDelegate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "delegates")
class Delegate(

    @Column(name = "public_key", nullable = false, unique = true)
    var publicKey: String,

    @Column(name = "address", nullable = false)
    var address: String,

    id: Int = 0

) : BaseModel(id) {

    companion object {
        fun of(delegateDto: NetworkDelegate): Delegate = Delegate(
            delegateDto.publicKey,
            delegateDto.address
        )
    }

}