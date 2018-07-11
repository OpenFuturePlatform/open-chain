package io.openfuture.chain.nio.converter

import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.protocol.CommunicationProtocol
import org.springframework.stereotype.Component

@Component
class TransactionConverter: MessageConverter<Transaction, CommunicationProtocol.Transaction> {

    override fun fromMessage(message: CommunicationProtocol.Transaction): Transaction {
        return Transaction(
            message.hash,
            message.amount,
            message.timestamp,
            message.recipientkey,
            message.senderKey,
            message.signature
        )
    }

    override fun fromEntity(entity: Transaction): CommunicationProtocol.Transaction {
        return CommunicationProtocol.Transaction.newBuilder()
            .setHash(entity.hash)
            .setAmount(entity.amount)
            .setTimestamp(entity.timestamp)
            .setRecipientkey(entity.recipientkey)
            .setSenderKey(entity.senderKey)
            .setSignature(entity.signature)
            .build()
    }

}