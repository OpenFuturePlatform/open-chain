package io.openfuture.chain.core.repository.jdbc

import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository


interface UTransactionsJdbcRepository {
    fun save(utransaction: UnconfirmedTransferTransaction): UnconfirmedTransferTransaction
}

@Repository
class UTransactionsJdbcRepositoryImpl(
    private val namedParameterJdbcOperations: NamedParameterJdbcOperations
) : UTransactionsJdbcRepository {

    override fun save(utransaction: UnconfirmedTransferTransaction): UnconfirmedTransferTransaction {
        val generatedKeyHolder = GeneratedKeyHolder()
        val sqlParameterSource = MapSqlParameterSource()

        sqlParameterSource.addValue("timestamp", utransaction.timestamp)
        sqlParameterSource.addValue("fee", utransaction.fee)
        sqlParameterSource.addValue("senderAddress", utransaction.senderAddress)
        sqlParameterSource.addValue("hash", utransaction.hash)
        sqlParameterSource.addValue("signature", utransaction.signature)
        sqlParameterSource.addValue("publicKey", utransaction.publicKey)

        namedParameterJdbcOperations.update(
            "INSERT INTO U_TRANSACTIONS(TIMESTAMP, FEE, SENDER_ADDRESS, HASH, SIGNATURE, SENDER_KEY) " +
                    "VALUES (:timestamp, :fee, :senderAddress, :hash, :signature, :publicKey)",
            sqlParameterSource, generatedKeyHolder
        )

        val id = generatedKeyHolder.key.toLong()

        saveUTransferTransaction(utransaction.getPayload(), id)

        utransaction.id = id
        return utransaction
    }

    private fun saveUTransferTransaction(t: TransferTransactionPayload, id: Long) {
        val sqlParameterSource = MapSqlParameterSource()

        sqlParameterSource.addValue("id", id)
        sqlParameterSource.addValue("amount", t.amount)
        sqlParameterSource.addValue("address", t.recipientAddress)
        sqlParameterSource.addValue("data", t.data)

        namedParameterJdbcOperations.update(
            "insert into U_TRANSACTIONS(ID, AMOUNT, RECIPIENT_ADDRESS, DATA) values (:id, amount, address, data)",
            sqlParameterSource
        )

    }


}