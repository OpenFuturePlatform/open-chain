package io.openfuture.chain.network.message.application.block

import io.netty.buffer.ByteBuf
import io.openfuture.chain.consensus.model.entity.transaction.DelegateTransaction
import io.openfuture.chain.consensus.model.entity.transaction.TransferTransaction
import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.network.message.BlockMessage
import io.openfuture.chain.network.message.application.transaction.DelegateTransactionMessage
import io.openfuture.chain.network.message.application.transaction.TransferTransactionMessage
import io.openfuture.chain.network.message.application.transaction.VoteTransactionMessage
import io.openfuture.chain.network.extension.readList
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeList
import io.openfuture.chain.network.extension.writeString

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

    constructor(block: MainBlock) : this(
        block.height,
        block.previousHash,
        block.timestamp,
        block.reward,
        block.hash,
        block.signature!!,
        block.publicKey,
        block.merkleHash,
        block.transactions.filterIsInstance(TransferTransaction::class.java).map { TransferTransactionMessage(it) }.toMutableList(),
        block.transactions.filterIsInstance(VoteTransaction::class.java).map { VoteTransactionMessage(it) }.toMutableList(),
        block.transactions.filterIsInstance(DelegateTransaction::class.java).map { DelegateTransactionMessage(it) }.toMutableList()
    )

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

    fun toEntity(): MainBlock = MainBlock(
        height,
        previousHash,
        blockTimestamp,
        reward,
        publicKey,
        merkleHash,
        mutableSetOf()).apply { signature = super.signature }


    override fun toString() = "NetworkMainBlock(hash=$hash)"

}
