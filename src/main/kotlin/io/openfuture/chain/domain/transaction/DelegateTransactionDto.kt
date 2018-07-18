package io.openfuture.chain.domain.transaction

import io.openfuture.chain.domain.delegate.DelegateDto
import io.openfuture.chain.entity.transaction.DelegateTransaction
import io.openfuture.chain.network.domain.NetworkAddress

class DelegateTransactionDto(timestamp: Long, amount: Double, recipientAddress: String, senderKey: String,
                             senderAddress: String, senderSignature: String, hash: String,
                             val delegateDto: DelegateDto
) : BaseTransactionDto(timestamp, amount, recipientAddress, senderKey, senderAddress, senderSignature,
    hash) {

    constructor(tx: DelegateTransaction) : this(
        tx.timestamp,
        tx.amount,
        tx.recipientAddress,
        tx.senderPublicKey,
        tx.senderAddress,
        tx.senderSignature,
        tx.hash,
        DelegateDto(
            tx.key,
            NetworkAddress(
                tx.host,
                tx.port
            )
        )
    )

}