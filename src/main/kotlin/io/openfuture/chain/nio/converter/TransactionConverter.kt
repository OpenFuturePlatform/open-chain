package io.openfuture.chain.nio.converter

import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.protocol.CommunicationProtocol
import org.springframework.stereotype.Component

@Component
class TransactionConverter {

    fun toTransaction(transaction: CommunicationProtocol.Transaction): Transaction {
        return Transaction(
            transaction.hash,
            transaction.amount,
            transaction.timestamp,
            transaction.recipientkey,
            transaction.senderKey,
            transaction.signature
        )
    }

    fun toTransactionProto(transaction: Transaction): CommunicationProtocol.Transaction {
        return CommunicationProtocol.Transaction.newBuilder()
            .setHash(transaction.hash)
            .setAmount(transaction.amount)
            .setTimestamp(transaction.timestamp)
            .setRecipientkey(transaction.recipientkey)
            .setSenderKey(transaction.senderKey)
            .setSignature(transaction.signature)
            .build()
    }

}