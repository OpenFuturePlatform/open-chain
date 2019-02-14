package io.openfuture.chain.core.model.entity.block

import io.openfuture.chain.core.model.entity.base.BaseModel
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "temporary_blocks")
class TemporaryBlock(

    @Column(name = "height", nullable = false)
    var height: Long,

    @Column(name = "block", nullable = false)
    var block: String

) : BaseModel()