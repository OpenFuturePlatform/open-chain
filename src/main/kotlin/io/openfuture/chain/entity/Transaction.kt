package io.openfuture.chain.entity

import io.openfuture.chain.entity.base.BaseModel
import io.openfuture.chain.entity.dictionary.Currency
import io.openfuture.chain.util.DictionaryUtils
import javax.persistence.*

/**
 * @author Homza Pavel
 */
@Entity
@Table(name = "transactions")
class Transaction (

        @ManyToOne
        @JoinColumn(name = "block_id", nullable = false)
        val block: Block,

        @Column(name = "hash", nullable = false)
        val hash: String,

        @Column(name = "amount", nullable = false)
        val amount: Int = 0,

        @Column(name = "currency_id", nullable = false)
        val currencyId: Int,

        @Column(name = "timestamp", nullable = false)
        val timestamp: Long,

        @Column(name = "recipient_key", nullable = false)
        val recipient_key: String,

        @Column(name = "sender_key", nullable = false)
        val senderKey: String,

        @Column(name = "signature", nullable = false)
        val signature: String

) : BaseModel() {

    fun getCurrency() = DictionaryUtils.valueOf(Currency::class.java, currencyId)

}