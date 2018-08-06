package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
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
    var voteTxs: List<VoteTransactionMessage>,
    var delegateTxs: List<DelegateTransactionMessage>,
    var transferTxs: List<TransferTransactionMessage>
) : BlockMessage(height, previousHash, timestamp, reward, hash, signature, publicKey) {

    constructor(block: MainBlock, voteTxs: List<VoteTransaction>, delegateTxs: List<DelegateTransaction>,
                transferTxs: List<TransferTransaction>) : this(
        block.height,
        block.previousHash,
        block.timestamp,
        block.reward,
        block.hash,
        block.signature,
        block.publicKey,
        block.payload.merkleHash,
        voteTxs.map { VoteTransactionMessage(it) },
        delegateTxs.map { DelegateTransactionMessage(it) },
        transferTxs.map { TransferTransactionMessage(it) }
    )

    fun getAllTransactions(): List<BaseTransactionMessage> {
        return voteTxs + delegateTxs + transferTxs
    }

    override fun read(buffer: ByteBuf) {
        super.read(buffer)

        merkleHash = buffer.readString()
        voteTxs = buffer.readList()
        delegateTxs = buffer.readList()
        transferTxs = buffer.readList()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)

        buffer.writeString(merkleHash)
        buffer.writeList(voteTxs)
        buffer.writeList(delegateTxs)
        buffer.writeList(transferTxs)
    }

    override fun toString() = "MainBlockMessage(hash=$hash)"

}
