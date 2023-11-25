package io.openfuture.chain.tendermint.repository

import io.openfuture.chain.core.model.entity.tendermint.TendermintTransaction
import io.openfuture.chain.core.model.entity.tendermint.TendermintTransferTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository

interface TendermintTransactionsJdbcRepository {
    fun save(tendermintTransferTransaction: TendermintTransferTransaction): TendermintTransaction
}

@Repository
class TendermintTransactionsJdbcRepositoryImpl(
    private val namedParameterJdbcOperations: NamedParameterJdbcOperations
): TendermintTransactionsJdbcRepository{
    override fun save(tendermintTransferTransaction: TendermintTransferTransaction): TendermintTransaction {
        val generatedKeyHolder = GeneratedKeyHolder()
        val sqlParameterSource = MapSqlParameterSource()

        sqlParameterSource.addValue("timestamp", tendermintTransferTransaction.timestamp)
        sqlParameterSource.addValue("fee", tendermintTransferTransaction.fee)
        sqlParameterSource.addValue("senderAddress", tendermintTransferTransaction.senderAddress)
        sqlParameterSource.addValue("hash",tendermintTransferTransaction.hash)
        sqlParameterSource.addValue("signature", tendermintTransferTransaction.signature)
        sqlParameterSource.addValue("publicKey", tendermintTransferTransaction.publicKey)

        namedParameterJdbcOperations.update(
            "INSERT INTO tendermint_transactions(timestamp, fee, sender_address, hash, signature, sender_key) " +
                    "VALUES (:timestamp, :fee, :senderAddress, :hash, :signature, :publicKey)",
            sqlParameterSource, generatedKeyHolder
        )

        val id = generatedKeyHolder.key.toLong()

        saveTransferTransaction(tendermintTransferTransaction.getPayload(), id)

        tendermintTransferTransaction.id = id
        return tendermintTransferTransaction
    }

    private fun saveTransferTransaction(t: TransferTransactionPayload, id: Long) {
        val sqlParameterSource = MapSqlParameterSource()

        sqlParameterSource.addValue("id", id)
        sqlParameterSource.addValue("amount", t.amount)
        sqlParameterSource.addValue("address", t.recipientAddress)
        sqlParameterSource.addValue("data", t.data)

        namedParameterJdbcOperations.update(
            "insert into tendermint_transfer_transactions(id, amount, recipient_address, data) values (:id, :amount, :address, :data)",
            sqlParameterSource
        )

    }

}