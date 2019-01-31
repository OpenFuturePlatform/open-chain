package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import io.openfuture.chain.network.extension.readNullableString
import io.openfuture.chain.network.extension.writeNullableString
import org.apache.commons.lang3.StringUtils.EMPTY
import java.nio.ByteBuffer
import kotlin.text.Charsets.UTF_8

@NoArgConstructor
class AccountStateMessage(
    address: String,
    var balance: Long,
    var voteFor: String? = null,
    var storage: String? = null
) : StateMessage(address) {

    override fun getBytes(): ByteArray {
        val voteForBytes = (voteFor ?: EMPTY).toByteArray(UTF_8)
        val storageBytes = (storage ?: EMPTY).toByteArray(UTF_8)
        return ByteBuffer.allocate(LONG_BYTES + voteForBytes.size + storageBytes.size)
            .putLong(balance)
            .put(voteForBytes)
            .put(storageBytes)
            .array()
    }

    override fun read(buf: ByteBuf) {
        super.read(buf)
        balance = buf.readLong()
        voteFor = buf.readNullableString()
        storage = buf.readNullableString()
    }

    override fun write(buf: ByteBuf) {
        super.write(buf)
        buf.writeLong(balance)
        buf.writeNullableString(voteFor)
        buf.writeNullableString(storage)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccountStateMessage) return false
        if (!super.equals(other)) return false

        if (balance != other.balance) return false
        if (voteFor != other.voteFor) return false
        if (storage != other.storage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + balance.hashCode()
        result = 31 * result + (voteFor?.hashCode() ?: 0)
        result = 31 * result + (storage?.hashCode() ?: 0)
        return result
    }

}