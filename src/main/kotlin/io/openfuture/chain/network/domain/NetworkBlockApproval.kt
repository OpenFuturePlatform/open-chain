package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import kotlin.text.Charsets.UTF_8

@NoArgConstructor
data class NetworkBlockApproval(
    var stageId: Int,
    var height: Long,
    var hash: String,
    var publicKey: String,
    var signature: String? = null
) : Packet() {

    fun getBytes(): ByteArray
        = StringBuilder().append(stageId).append(height).append(hash).append(publicKey).toString().toByteArray()

    override fun readParams(buffer: ByteBuf) {
        stageId = buffer.readInt()
        height = buffer.readLong()
        hash = buffer.readString()
        publicKey = buffer.readString()
        signature = buffer.readString()
    }

    override fun writeParams(buffer: ByteBuf) {
        buffer.writeInt(stageId)
        buffer.writeLong(height)
        buffer.writeInt(hash.length)
        buffer.writeCharSequence(hash, UTF_8)
        buffer.writeInt(publicKey.length)
        buffer.writeCharSequence(publicKey, UTF_8)
        buffer.writeInt(signature!!.length)
        buffer.writeCharSequence(signature, UTF_8)
    }

}