package io.openfuture.chain.domain.transaction

abstract class BaseTransactionDto(
    var timestamp: Long,
    var amount: Long,
    var recipientKey: String,
    var recipientAddress: String,
    var senderKey: String,
    var senderAddress: String,
    var senderSignature: String,
    var hash: String
)
