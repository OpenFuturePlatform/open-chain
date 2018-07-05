package io.openfuture.chain.entity

import io.openfuture.chain.entity.base.BaseModel
import io.openfuture.chain.entity.dictionary.TransactionType
import io.openfuture.chain.util.DictionaryUtils
import javax.persistence.*

@Entity
@Table(name = "transactions")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Transaction(

        @Column(name = "timestamp", nullable = false)
        var timestamp: Long,

        @Column(name = "hash", nullable = false)
        var hash: String,

        @Column(name = "type_id", nullable = false)
        var typeId: Int,

        @ManyToOne
        @JoinColumn(name = "block_id", nullable = true)
        var block: Block? = null

) : BaseModel() {

    fun getType(): TransactionType {
        return DictionaryUtils.valueOf(TransactionType::class.java, typeId)
    }

}