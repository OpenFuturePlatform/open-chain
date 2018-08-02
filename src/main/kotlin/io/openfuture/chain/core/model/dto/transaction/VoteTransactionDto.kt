package io.openfuture.chain.core.model.dto.transaction

class VoteTransactionDto(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,
    var voteTypeId: Int,
    var delegateKey: String
) : BaseTransactionDto(timestamp, fee, senderAddress, senderPublicKey, senderSignature, hash)