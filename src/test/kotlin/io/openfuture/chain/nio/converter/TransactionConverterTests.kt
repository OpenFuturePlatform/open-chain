package io.openfuture.chain.nio.converter

import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.protocol.CommunicationProtocol
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class TransactionConverterTests {

    private lateinit var transactionConverter: TransactionConverter


    @Before
    fun setUp() {
        transactionConverter = TransactionConverter()
    }

    @Test
    fun fromEntityShouldReturnTransactionMessage() {
        val hash = "hash"
        val amount = 1
        val timestamp = 1L
        val recipientKey = "recipientKey"
        val senderKey = "senderKey"
        val signature = "signature"
        val senderAddress = "senderAddress"
        val recipientAddress = "recipientAddress"
        val transaction = Transaction(
            hash,
            amount,
            timestamp,
            recipientKey,
            senderKey,
            signature,
            senderAddress,
            recipientAddress
        )

        val transactionMessage = transactionConverter.fromEntity(transaction)

        assertTransaction(transactionMessage, transaction)
    }

    @Test
    fun fromMessageShouldReturnTransactionEntity() {
        val hash = "hash"
        val amount = 1
        val timestamp = 1L
        val recipientKey = "recipientKey"
        val senderKey = "senderKey"
        val signature = "signature"
        val senderAddress = "senderAddress"
        val recipientAddress = "recipientAddress"
        val transactionMessage = createTransactionMessage(
            hash,
            amount,
            timestamp,
            recipientKey,
            senderKey,
            signature,
            senderAddress,
            recipientAddress
        )

        val transaction = transactionConverter.fromMessage(transactionMessage)

        assertTransaction(transactionMessage, transaction)
    }

    private fun createTransactionMessage(
        hash: String,
        amount: Int,
        timestamp: Long,
        recipientKey: String,
        senderKey: String,
        signature: String,
        senderAddress: String,
        recipientAddress: String
    ): CommunicationProtocol.Transaction {
        return CommunicationProtocol.Transaction.newBuilder()
            .setHash(hash)
            .setAmount(amount)
            .setTimestamp(timestamp)
            .setRecipientKey(recipientKey)
            .setSenderKey(senderKey)
            .setSignature(signature)
            .setSenderAddress(senderAddress)
            .setRecipientAddress(recipientAddress)
            .build()
    }

    private fun assertTransaction(transactionMessage: CommunicationProtocol.Transaction, transaction: Transaction) {
        assertThat(transactionMessage.hash).isEqualTo(transaction.hash)
        assertThat(transactionMessage.amount).isEqualTo(transaction.amount)
        assertThat(transactionMessage.timestamp).isEqualTo(transaction.timestamp)
        assertThat(transactionMessage.recipientKey).isEqualTo(transaction.recipientKey)
        assertThat(transactionMessage.senderKey).isEqualTo(transaction.senderKey)
        assertThat(transactionMessage.signature).isEqualTo(transaction.signature)
        assertThat(transactionMessage.senderAddress).isEqualTo(transaction.senderAddress)
        assertThat(transactionMessage.recipientAddress).isEqualTo(transaction.recipientAddress)
    }

}