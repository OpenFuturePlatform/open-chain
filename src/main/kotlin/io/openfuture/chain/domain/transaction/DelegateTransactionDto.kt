package io.openfuture.chain.domain.transaction

import io.openfuture.chain.domain.delegate.DelegateDto
import io.openfuture.chain.domain.transaction.data.DelegateTransactionData
import io.openfuture.chain.entity.transaction.DelegateTransaction
import io.openfuture.chain.network.domain.NetworkAddress

class DelegateTransactionDto(
    data: DelegateTransactionData,
    timestamp: Long,
    senderPublicKey: String,
    senderSignature: String,
    hash: String
) : BaseTransactionDto<DelegateTransactionData>(data, timestamp, senderPublicKey, senderSignature, hash) {

    constructor(tx: DelegateTransaction) : this(
        DelegateTransactionData(DelegateDto(tx.delegateKey, tx.delegateAddress,NetworkAddress(tx.host, tx.port))),
        tx.timestamp,
        tx.senderPublicKey,
        tx.senderSignature,
        tx.hash
    )

}