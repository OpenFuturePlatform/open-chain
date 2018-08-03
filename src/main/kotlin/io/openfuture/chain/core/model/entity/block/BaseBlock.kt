package io.openfuture.chain.core.model.entity.block

import io.openfuture.chain.core.model.entity.base.BaseModel
import io.openfuture.chain.core.model.entity.block.payload.BaseBlockPayload
import io.openfuture.chain.core.model.entity.transaction.payload.BaseTransactionPayload
import javax.persistence.*

@Entity
@Table(name = "blocks")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class BaseBlock(

    @Column(name = "timestamp", nullable = false)
    var timestamp: Long,

    @Column(name = "height", nullable = false)
    var height: Long,

    @Column(name = "hash", nullable = false, unique = true)
    var hash: String,

    @Column(name = "signature", nullable = false)
    var signature: String,

    @Column(name = "public_key", nullable = false)
    var publicKey: String

) : BaseModel() {

    abstract fun getPayload(): BaseBlockPayload

}
