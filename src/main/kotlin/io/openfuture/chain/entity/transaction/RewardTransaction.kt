package io.openfuture.chain.entity.transaction

import io.openfuture.chain.entity.MainBlock
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "reward_transactions")
class RewardTransaction(
    timestamp: Long,
    amount: Long,
    fee: Long,
    recipientAddress: String,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,
    block: MainBlock? = null
) : BaseTransaction(timestamp, amount, fee, recipientAddress, senderAddress, senderPublicKey, senderSignature, hash, block)