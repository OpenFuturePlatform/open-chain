package io.openfuture.chain.network.domain

import io.openfuture.chain.entity.transaction.TransferTransaction

class NetworkTransferTransaction() : NetworkTransaction() {

    constructor(transaction: TransferTransaction) : this() {
        timestamp = transaction.timestamp
        amount = transaction.amount
        fee = transaction.fee
        recipientAddress = transaction.recipientAddress
        senderKey = transaction.senderKey
        senderAddress = transaction.senderAddress
        senderSignature = transaction.senderSignature!!
        hash = transaction.hash
    }

}
