package io.openfuture.chain.entity.transaction.unconfirmed

import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "u_reward_transactions")
class URewardTransaction(
    timestamp: Long,
    amount: Long,
    fee: Long,
    recipientAddress: String,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String
) : UTransaction(timestamp, amount, fee, recipientAddress, senderAddress, senderPublicKey, senderSignature, hash)