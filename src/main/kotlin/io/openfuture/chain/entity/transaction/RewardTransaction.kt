package io.openfuture.chain.entity.transaction

import io.openfuture.chain.entity.MainBlock
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "reward_transactions")
class RewardTransaction(
    timestamp: Long,
    amount: Double,
    fee: Double,
    recipientAddress: String,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,

    @Column(name = "hash_created_block", nullable = false, unique = true)
    val hashCreatedBlock: String,

    block: MainBlock? = null
) : BaseTransaction(timestamp, amount, fee, recipientAddress, senderAddress, senderPublicKey, senderSignature, hash, block)