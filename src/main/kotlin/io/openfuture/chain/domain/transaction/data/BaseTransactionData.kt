package io.openfuture.chain.domain.transaction.data

import io.netty.buffer.ByteBuf
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.network.domain.NetworkEntity
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

abstract class BaseTransactionData(
    var amount: Long,
    var fee: Long,
    var recipientAddress: String,
    var senderAddress: String
) : NetworkEntity() {

    abstract fun getBytes(): ByteArray

    fun getHash(): String = HashUtils.toHexString(HashUtils.sha256(getBytes()))

    override fun read(buffer: ByteBuf) {
        amount = buffer.readLong()
        fee = buffer.readLong()
        recipientAddress = buffer.readString()
        senderAddress = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeLong(amount)
        buffer.writeLong(fee)
        buffer.writeString(recipientAddress)
        buffer.writeString(senderAddress)
    }

}