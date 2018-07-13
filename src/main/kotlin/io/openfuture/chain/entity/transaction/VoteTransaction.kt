package io.openfuture.chain.entity.transaction

import io.openfuture.chain.domain.rpc.transaction.VoteTransactionRequest
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.util.DictionaryUtils
import javax.persistence.*

@Entity
@Table(name = "vote_transactions")
class VoteTransaction(timestamp: Long, amount: Double, recipientAddress: String, senderKey: String,
                      senderAddress: String, senderSignature: String, hash: String,

    @Column(name = "vote_type_id", nullable = false)
    private var voteTypeId: Int,

    @Column(name = "delegate_host", nullable = false)
    var delegateHost: String,

    @Column(name = "delegate_port", nullable = false)
    var delegatePort: Int,

    block: Block? = null

) : BaseTransaction(timestamp, amount, recipientAddress, senderKey, senderAddress,
    senderSignature, hash, block) {

    companion object {
        fun of(dto: VoteTransactionDto): VoteTransaction = VoteTransaction(
            dto.timestamp,
            dto.amount,
            dto.recipientAddress,
            dto.senderKey,
            dto.senderAddress,
            dto.senderSignature,
            dto.hash,
            dto.voteType.getId(),
            dto.delegateInfo.networkAddress.host,
            dto.delegateInfo.networkAddress.port
        )

        fun of(timestamp: Long, request: VoteTransactionRequest): VoteTransaction = VoteTransaction(
            timestamp,
            request.amount!!,
            request.recipientAddress!!,
            request.senderKey!!,
            request.senderAddress!!,
            request.senderSignature!!,
            request.getHash(),
            request.voteType!!.getId(),
            request.delegateInfo!!.networkAddress.host,
            request.delegateInfo!!.networkAddress.port

        )
    }

    fun getVoteType() = DictionaryUtils.valueOf(VoteType::class.java, voteTypeId)

    fun setVoteType(voteType: VoteType) {
        voteTypeId = voteType.getId()
    }

}