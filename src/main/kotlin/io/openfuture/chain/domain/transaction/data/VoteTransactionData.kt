package io.openfuture.chain.domain.transaction.data

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
class VoteTransactionData(
    amount: Long,
    fee: Long,
    recipientAddress: String,
    senderAddress: String,
    var voteTypeId: Int,
    var delegateKey: String
) : BaseTransactionData(amount, fee, recipientAddress, senderAddress) {

    override fun getBytes(): ByteArray {
        val builder = StringBuilder()
        builder.append(amount)
        builder.append(fee)
        builder.append(recipientAddress)
        builder.append(senderAddress)
        builder.append(voteTypeId)
        builder.append(delegateKey)
        return builder.toString().toByteArray()
    }

    override fun read(buffer: ByteBuf) {
        super.read(buffer)

        voteTypeId = buffer.readInt()
        delegateKey = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)

        buffer.writeInt(voteTypeId)
        buffer.writeString(delegateKey)
    }

}