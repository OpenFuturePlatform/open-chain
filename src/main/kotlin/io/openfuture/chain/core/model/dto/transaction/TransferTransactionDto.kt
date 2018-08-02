package io.openfuture.chain.core.model.dto.transaction

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
