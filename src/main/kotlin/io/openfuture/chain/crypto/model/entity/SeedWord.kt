package io.openfuture.chain.crypto.model.entity

import io.openfuture.chain.core.model.entity.base.BaseModel
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "seed_words")
class SeedWord(

    @Column(name = "ind", nullable = false)
    var index: Int,

    @Column(name = "value", nullable = false)
    var value: String

) : BaseModel()