package io.openfuture.chain.network.message.consensus

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.readStringList
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.extension.writeStringList
import io.openfuture.chain.network.message.core.BlockMessage

@NoArgConstructor
class PendingBlockMessage(
    height: Long,
    previousHash: String,
    blockTimestamp: Long,
    reward: Long,
    hash: String,
    signature: String,
    publicKey: String,
    var merkleHash: String,
    var transactions: List<String>
) : BlockMessage(height, previousHash, blockTimestamp, reward, publicKey, hash, signature) {

    override fun read(buffer: ByteBuf) {
        super.read(buffer)

        merkleHash = buffer.readString()
        transactions = buffer.readStringList()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)

        buffer.writeString(merkleHash)
        buffer.writeStringList(transactions)
    }

    override fun toString() = "PendingBlockMessage(hash=$hash)"

}
