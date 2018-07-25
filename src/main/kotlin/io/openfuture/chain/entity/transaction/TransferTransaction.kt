package io.openfuture.chain.entity.transaction

import io.openfuture.chain.entity.block.MainBlock
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "transfer_transactions")
class TransferTransaction(
    timestamp: Long,
    amount: Long,
    recipientAddress: String,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,
    block: MainBlock? = null
) : Transaction(timestamp, amount, recipientAddress, senderAddress, senderPublicKey, senderSignature, hash, block)