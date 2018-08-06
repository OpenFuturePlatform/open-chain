package io.openfuture.chain.network.message.consensus

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.readStringList
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.extension.writeStringList
import io.openfuture.chain.network.message.core.BlockMessage

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
    var voteTxs: List<String>,
    var delegateTxs: List<String>,
    var transferTxs: List<String>
) : BlockMessage(height, previousHash, timestamp, reward, hash, signature, publicKey) {

    constructor(block: MainBlock, voteTxs: List<UnconfirmedVoteTransaction>, delegateTxs: List<UnconfirmedDelegateTransaction>,
                transferTxs: List<UnconfirmedTransferTransaction>) : this(
        block.height,
        block.previousHash,
        block.timestamp,
        block.reward,
        block.hash,
        block.signature,
        block.publicKey,
        block.payload.merkleHash,
        voteTxs.map { it.hash },
        delegateTxs.map { it.hash },
        transferTxs.map { it.hash }
    )

    fun getAllTransactions(): List<String> {
        return voteTxs + delegateTxs + transferTxs
    }

    override fun read(buffer: ByteBuf) {
        super.read(buffer)

        merkleHash = buffer.readString()
        voteTxs = buffer.readStringList()
        delegateTxs = buffer.readStringList()
        transferTxs = buffer.readStringList()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)

        buffer.writeString(merkleHash)
        buffer.writeStringList(voteTxs)
        buffer.writeStringList(delegateTxs)
        buffer.writeStringList(transferTxs)
    }

    override fun toString() = "MainBlockMessage(hash=$hash)"

}