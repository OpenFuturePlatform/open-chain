package io.openfuture.chain.entity

import io.openfuture.chain.entity.base.BaseModel
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "seed_words")
class SeedWord(

    @Column(name = "word_index", nullable = false)
    var wordIndex: Int,

    @Column(name = "word_value", nullable = false)
    var wordValue: String

) : BaseModel()