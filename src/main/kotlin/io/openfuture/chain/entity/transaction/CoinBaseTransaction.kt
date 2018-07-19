package io.openfuture.chain.entity.transaction

import io.openfuture.chain.domain.transaction.CoinBaseTransactionDto
import io.openfuture.chain.entity.MainBlock
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "coinbase_transactions")
class CoinBaseTransaction(
    timestamp: Long,
    amount: Double,
    fee: Double,
    recipientAddress: String,
    senderKey: String,
    senderAddress: String,
    senderSignature: String,
    hash: String,
    block: MainBlock? = null
) : BaseTransaction(timestamp, amount, fee, recipientAddress, senderKey, senderAddress, senderSignature, hash, block) {

    companion object {
        fun of(dto: CoinBaseTransactionDto): CoinBaseTransaction = CoinBaseTransaction(
            dto.timestamp,
            dto.amount,
            dto.fee,
            dto.recipientAddress,
            dto.senderKey,
            dto.senderAddress,
            dto.senderSignature,
            dto.hash
        )
    }

}