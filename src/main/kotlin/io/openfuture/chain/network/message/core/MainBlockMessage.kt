package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.*

@NoArgConstructor
class MainBlockMessage(
    height: Long,
    previousHash: String,
    timestamp: Long,
    reward: Long,
    hash: String,
    signature: String,
    publicKey: String,
    var merkleHash: String,
    var transactions: List<BaseTransactionMessage>
) : BlockMessage(height, previousHash, timestamp, reward, hash, signature, publicKey) {

    constructor(block: MainBlock, transactions: List<BaseTransactionMessage>) : this(
        block.height,
        block.previousHash,
        block.timestamp,
        block.reward,
        block.hash,
        block.signature,
        block.publicKey,
        block.payload.merkleHash,
        transactions
    )

    override fun read(buffer: ByteBuf) {
        super.read(buffer)

        merkleHash = buffer.readString()
        transactions = buffer.readList()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)

        buffer.writeString(merkleHash)
        buffer.writeList(transactions)
    }

    override fun toString() = "MainBlockMessage(hash=$hash)"

}
