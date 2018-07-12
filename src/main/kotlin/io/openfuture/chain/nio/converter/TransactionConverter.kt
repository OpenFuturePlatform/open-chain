package io.openfuture.chain.nio.converter

import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.protocol.CommunicationProtocol
import org.springframework.stereotype.Component

@Component
class TransactionConverter: MessageConverter<Transaction, CommunicationProtocol.Transaction> {

    private val transactionBuilder = CommunicationProtocol.Transaction.newBuilder()

    override fun fromMessage(message: CommunicationProtocol.Transaction): Transaction {
        return Transaction(
            message.hash,
            message.amount,
            message.timestamp,
            message.recipientkey,
            message.senderKey,
            message.signature,
            message.senderAddress,
            message.recipientAddress
        )
    }

    override fun fromEntity(entity: Transaction): CommunicationProtocol.Transaction {
        return transactionBuilder
            .setHash(entity.hash)
            .setAmount(entity.amount)
            .setTimestamp(entity.timestamp)
            .setRecipientkey(entity.recipientKey)
            .setSenderKey(entity.senderKey)
            .setSignature(entity.signature)
            .build()
    }

}