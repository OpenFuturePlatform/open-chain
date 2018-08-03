package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readList
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeList
import io.openfuture.chain.network.extension.writeString

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
    var transactions: List<String>
) : BlockMessage(height, previousHash, timestamp, reward, hash, signature, publicKey) {

    constructor(block: MainBlock) : this(
        block.height,
        block.getPayload().previousHash,
        block.timestamp,
        block.getPayload().reward,
        block.hash,
        block.signature,
        block.publicKey,
        block.getPayload().merkleHash,
        block.getPayload().transactions.map { it.hash }
    )

    override fun read(buffer: ByteBuf) {
        super.read(buffer)

        merkleHash = buffer.readString()
//        transferTransactions = buffer.readList()
//        voteTransactions = buffer.readList()
//        delegateTransactions = buffer.readList()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)

        buffer.writeString(merkleHash)
//        buffer.writeList(transferTransactions)
//        buffer.writeList(voteTransactions)
//        buffer.writeList(delegateTransactions)
    }

    override fun toString() = "MainBlockMessage(hash=$hash)"

}
