package io.openfuture.chain.consensus.model.entity.transaction

import io.openfuture.chain.consensus.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.Transaction
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "transfer_transactions")
class TransferTransaction(
    timestamp: Long,
    amount: Long,
    fee: Long,
    recipientAddress: String,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,
    block: MainBlock? = null
) : Transaction(timestamp, amount, fee, recipientAddress, senderAddress, senderPublicKey, senderSignature, hash, block)