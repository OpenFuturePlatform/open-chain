package io.openfuture.chain.core.model.dto.transaction

import io.openfuture.chain.consensus.annotation.NoArgConstructor
import io.openfuture.chain.core.model.dto.transaction.data.TransferTransactionData
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransferTransaction

class TransferTransactionDto(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,
    var amount: Long,
    var recipientAddress: String
) : BaseTransactionDto(timestamp, fee, senderAddress, senderPublicKey, senderSignature, hash)
