package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor

@NoArgConstructor
class DeployTransactionMessage(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    hash: String,
    senderSignature: String,
    senderPublicKey: String,
    var bytecode: ByteArray
) : TransactionMessage(timestamp, fee, senderAddress, hash, senderSignature, senderPublicKey) {

    override fun read(buf: ByteBuf) {
        super.read(buf)
        buf.readBytes(bytecode)
    }

    override fun write(buf: ByteBuf) {
        super.write(buf)
        buf.writeBytes(bytecode)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DeployTransactionMessage) return false
        if (!super.equals(other)) return false

        if (!bytecode.contentEquals(other.bytecode)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + bytecode.contentHashCode()
        return result
    }


}