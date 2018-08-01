package io.openfuture.chain.core.model.dto.transaction.data

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
class DelegateTransactionData(
    amount: Long,
    fee: Long,
    recipientAddress: String,
    senderAddress: String,
    var delegateKey: String
) : BaseTransactionData(amount, fee, recipientAddress, senderAddress) {

    override fun getBytes(): ByteArray {
        val builder = StringBuilder()
        builder.append(amount)
        builder.append(fee)
        builder.append(recipientAddress)
        builder.append(senderAddress)
        builder.append(delegateKey)
        return builder.toString().toByteArray()
    }

    override fun read(buffer: ByteBuf) {
        super.read(buffer)

        delegateKey = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)

        buffer.writeString(delegateKey)
    }

}