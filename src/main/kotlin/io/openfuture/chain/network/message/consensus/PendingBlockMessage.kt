package io.openfuture.chain.network.message.consensus

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
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
    timestamp: Long,
    reward: Long,
    hash: String,
    signature: String,
    publicKey: String,
    var merkleHash: String,
    var voteTransactions: List<String>,
    var delegateTransactions: List<String>,
    var transferTransactions: List<String>
) : BlockMessage(height, previousHash, timestamp, reward, hash, signature, publicKey) {

    constructor(block: MainBlock, voteTransactions: List<UnconfirmedVoteTransaction>, delegateTransactions: List<UnconfirmedDelegateTransaction>,
                transferTransactions: List<UnconfirmedTransferTransaction>) : this(
        block.height,
        block.previousHash,
        block.timestamp,
        block.reward,
        block.hash,
        block.signature,
        block.publicKey,
        block.payload.merkleHash,
        voteTransactions.map { it.hash },
        delegateTransactions.map { it.hash },
        transferTransactions.map { it.hash }
    )

    fun getAllTransactions(): List<String> {
        return voteTransactions + delegateTransactions + transferTransactions
    }

    override fun read(buffer: ByteBuf) {
        super.read(buffer)

        merkleHash = buffer.readString()
        voteTransactions = buffer.readStringList()
        delegateTransactions = buffer.readStringList()
        transferTransactions = buffer.readStringList()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)

        buffer.writeString(merkleHash)
        buffer.writeStringList(voteTransactions)
        buffer.writeStringList(delegateTransactions)
        buffer.writeStringList(transferTransactions)
    }

    override fun toString() = "PendingBlockMessage(hash=$hash)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PendingBlockMessage

        if (merkleHash != other.merkleHash) return false
        if (voteTransactions != other.voteTransactions) return false
        if (delegateTransactions != other.delegateTransactions) return false
        if (transferTransactions != other.transferTransactions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = merkleHash.hashCode()
        result = 31 * result + voteTransactions.hashCode()
        result = 31 * result + delegateTransactions.hashCode()
        result = 31 * result + transferTransactions.hashCode()
        return result
    }

}