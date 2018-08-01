package io.openfuture.chain.network.domain.application.transaction

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor
import io.openfuture.chain.network.domain.application.transaction.data.RewardTransactionData
import io.openfuture.chain.entity.transaction.RewardTransaction
import io.openfuture.chain.network.domain.base.BaseMessage
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
class RewardTransactionMessage(
    var data: RewardTransactionData,
    var timestamp: Long,
    var senderPublicKey: String,
    var senderSignature: String,
    var hash: String
) : BaseMessage {

    constructor(tx: RewardTransaction) : this(
        RewardTransactionData(tx.amount, tx.fee, tx.recipientAddress, tx.senderAddress),
        tx.timestamp,
        tx.senderPublicKey,
        tx.senderSignature,
        tx.hash
    )
    override fun read(buffer: ByteBuf) {
        data = RewardTransactionData::class.java.newInstance()
        data.read(buffer)

        timestamp = buffer.readLong()
        senderPublicKey = buffer.readString()
        senderSignature = buffer.readString()
        hash = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        data.write(buffer)

        buffer.writeLong(timestamp)
        buffer.writeString(senderPublicKey)
        buffer.writeString(senderSignature)
        buffer.writeString(hash)
    }

    fun toEntity(): RewardTransaction = RewardTransaction (
        timestamp,
        data.amount,
        data.fee,
        data.recipientAddress,
        data.senderAddress,
        senderPublicKey,
        senderSignature,
        hash
    )

}
