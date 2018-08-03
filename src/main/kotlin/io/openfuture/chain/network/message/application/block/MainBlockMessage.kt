package io.openfuture.chain.network.message.application.block

import io.netty.buffer.ByteBuf
import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readList
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeList
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.message.application.transaction.DelegateTransactionMessage
import io.openfuture.chain.network.message.application.transaction.TransferTransactionMessage
import io.openfuture.chain.network.message.application.transaction.VoteTransactionMessage

@NoArgConstructor
class MainBlockMessage(
    height: Long,
    previousHash: String,
    blockTimestamp: Long,
    reward: Long,
    hash: String,
    signature: String,
    publicKey: String,
    var merkleHash: String,
    var transferTransactions: MutableList<TransferTransactionMessage>,
    var voteTransactions: MutableList<VoteTransactionMessage>,
    var delegateTransactions: MutableList<DelegateTransactionMessage>
) : BlockMessage(height, previousHash, blockTimestamp, reward, publicKey, hash, signature) {

    override fun read(buffer: ByteBuf) {
        super.read(buffer)

        merkleHash = buffer.readString()
        transferTransactions = buffer.readList()
        voteTransactions = buffer.readList()
        delegateTransactions = buffer.readList()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)

        buffer.writeString(merkleHash)
        buffer.writeList(transferTransactions)
        buffer.writeList(voteTransactions)
        buffer.writeList(delegateTransactions)
    }

    override fun toString() = "MainBlockMessage(hash=$hash)"

}
