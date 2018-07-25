package io.openfuture.chain.entity.transaction

import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.rpc.transaction.VoteTransactionRequest
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.network.domain.NetworkVoteTransaction
import io.openfuture.chain.util.DictionaryUtils
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "vote_transactions")
class VoteTransaction(
    timestamp: Long,
    amount: Double,
    fee: Double,
    recipientAddress: String,
    senderKey: String,
    senderAddress: String,

    @Column(name = "vote_type_id", nullable = false)
    private var voteTypeId: Int,

    @Column(name = "delegate_host", nullable = false)
    var delegateHost: String,

    @Column(name = "delegate_port", nullable = false)
    var delegatePort: Int,

    hash: String? = null,
    senderSignature: String? = null,
    block: MainBlock? = null

) : BaseTransaction(timestamp, amount, fee, recipientAddress, senderKey, senderAddress,
    hash ?: HashUtils.toHexString(HashUtils.sha256(
        (senderAddress + recipientAddress + timestamp + amount + fee + voteTypeId + delegateHost + delegatePort).toByteArray())),
    senderSignature, block) {

    companion object {
        fun of(dto: VoteTransactionDto): VoteTransaction = VoteTransaction(
            dto.timestamp,
            dto.amount,
            dto.fee,
            dto.recipientAddress,
            dto.senderKey,
            dto.senderAddress,
            dto.voteType.getId(),
            dto.delegateInfo.networkAddress.host,
            dto.delegateInfo.networkAddress.port,
            dto.hash,
            dto.senderSignature
        )

        fun of(timestamp: Long, request: VoteTransactionRequest): VoteTransaction = VoteTransaction(
            timestamp,
            request.amount!!,
            request.fee!!,
            request.recipientAddress!!,
            request.senderKey!!,
            request.senderAddress!!,
            request.voteType!!.getId(),
            request.delegateInfo!!.networkAddress.host,
            request.delegateInfo!!.networkAddress.port,
            request.getHash(),
            request.senderSignature!!
        )

        fun of(dto: NetworkVoteTransaction): VoteTransaction = VoteTransaction(
            dto.timestamp,
            dto.amount,
            dto.fee,
            dto.recipientAddress,
            dto.senderKey,
            dto.senderAddress,
            dto.voteTypeId,
            dto.delegateHost,
            dto.delegatePort,
            null,
            dto.senderSignature
        )

    }

    fun getVoteType() = DictionaryUtils.valueOf(VoteType::class.java, voteTypeId)

    fun setVoteType(voteType: VoteType) {
        voteTypeId = voteType.getId()
    }

}