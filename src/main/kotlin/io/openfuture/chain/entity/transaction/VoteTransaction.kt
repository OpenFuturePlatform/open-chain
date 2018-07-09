package io.openfuture.chain.entity.transaction

import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.entity.block.Block
import io.openfuture.chain.entity.dictionary.TransactionType
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.util.DictionaryUtils
import javax.persistence.*

@Entity
@Table(name = "vote_transactions")
class VoteTransaction(
        timestamp: Long,
        amount: Long,
        recipientKey: String,
        senderKey: String,
        senderSignature: String,
        hash: String,

        @Column(name = "vote_type_id")
        var voteTypeId: Int,

        @Column(name = "delegate_key", nullable = false)
        var delegateKey: String,

        @Column(name = "weight", nullable = false)
        var weight: Int,

        block: Block? = null

) : Transaction(TransactionType.VOTE.getId(), timestamp, amount, recipientKey, senderKey, senderSignature, hash,
        block) {

    override fun toDto(): VoteTransactionDto = VoteTransactionDto(
            this.timestamp,
            this.amount,
            this.recipientKey,
            this.senderKey,
            this.senderSignature,
            this.hash,
            DictionaryUtils.valueOf(VoteType::class.java, this.voteTypeId),
            this.delegateKey,
            this.weight
    )

}