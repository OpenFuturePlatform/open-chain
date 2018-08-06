package io.openfuture.chain.network.message.consensus

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.BaseTransaction
import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.readStringList
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.extension.writeStringList
import io.openfuture.chain.network.message.core.BlockMessage
import io.openfuture.chain.network.message.core.MainBlockMessage

@NoArgConstructor
class PendingBlockMessage(
    height: Long,
    previousHash: String,
    timestamp: Long,
    reward: Long,
    hash: String,
    signature: String,
    publicKey: String,
    var merkleHash: String,
    var transactions: List<String>
) : BlockMessage(height, previousHash, timestamp, reward, hash, signature, publicKey) {

    constructor(block: MainBlock, transactions: List<BaseTransaction>) : this(
        block.height,
        block.previousHash,
        block.timestamp,
        block.reward,
        block.hash,
        block.signature,
        block.publicKey,
        block.payload.merkleHash,
        transactions.map { it.hash }
    )

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

    override fun toString() = "MainBlockMessage(hash=$hash)"

}