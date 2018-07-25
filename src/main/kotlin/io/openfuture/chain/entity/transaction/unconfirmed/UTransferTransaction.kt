package io.openfuture.chain.entity.transaction.unconfirmed

import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "u_transfer_transactions")
class UTransferTransaction(
    timestamp: Long,
    amount: Long,
    recipientAddress: String,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String
) : UTransaction(timestamp, amount, recipientAddress, senderAddress, senderPublicKey, senderSignature, hash)