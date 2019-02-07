package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
class DelegateStateMessage(
    address: String,
    hash: String,
    var rating: Long,
    var walletAddress: String,
    var createDate: Long
) : StateMessage(address, hash) {

    override fun read(buf: ByteBuf) {
        super.read(buf)
        rating = buf.readLong()
        walletAddress = buf.readString()
        createDate = buf.readLong()
    }

    override fun write(buf: ByteBuf) {
        super.write(buf)
        buf.writeLong(rating)
        buf.writeString(walletAddress)
        buf.writeLong(createDate)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DelegateStateMessage) return false
        if (!super.equals(other)) return false

        if (rating != other.rating) return false
        if (walletAddress != other.walletAddress) return false
        if (createDate != other.createDate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + rating.hashCode()
        result = 31 * result + walletAddress.hashCode()
        result = 31 * result + createDate.hashCode()
        return result
    }

}