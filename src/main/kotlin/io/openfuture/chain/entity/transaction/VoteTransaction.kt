package io.openfuture.chain.entity.transaction

import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.entity.Block
import javax.persistence.*

@Entity
@Table(name = "vote_transactions")
class VoteTransaction(
    timestamp: Long,
    amount: Double,
    recipientKey: String,
    recipientAddress: String,
    senderKey: String,
    senderAddress: String,
    senderSignature: String,
    hash: String,

    @Column(name = "vote_type_id", nullable = false)
    var voteTypeId: Int,

    @Column(name = "delegate_host", nullable = false)
    var delegateHost: String,

    @Column(name = "delegate_port", nullable = false)
    var delegatePort: Int,

    block: Block? = null

) : BaseTransaction(timestamp, amount, recipientKey, recipientAddress, senderKey, senderAddress,
    senderSignature, hash, block) {

    companion object {
        fun of(dto: VoteTransactionDto): VoteTransaction = VoteTransaction(
            dto.timestamp,
            dto.amount,
            dto.recipientKey,
            dto.recipientAddress,
            dto.senderKey,
            dto.senderAddress,
            dto.senderSignature,
            dto.hash,
            dto.voteType.getId(),
            dto.delegateInfo.host,
            dto.delegateInfo.port
        )
    }

}