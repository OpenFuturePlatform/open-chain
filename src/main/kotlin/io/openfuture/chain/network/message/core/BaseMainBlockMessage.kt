package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readList
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeList
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
abstract class BaseMainBlockMessage(
    height: Long,
    previousHash: String,
    timestamp: Long,
    hash: String,
    signature: String,
    publicKey: String,
    var merkleHash: String,
    var stateHash: String,
    var rewardTransaction: RewardTransactionMessage,
    var voteTransactions: List<VoteTransactionMessage>,
    var delegateTransactions: List<DelegateTransactionMessage>,
    var transferTransactions: List<TransferTransactionMessage>,
    var delegateStates: List<DelegateStateMessage>,
    var accountStates: List<AccountStateMessage>
) : BlockMessage(height, previousHash, timestamp, hash, signature, publicKey) {

    fun getAllTransactions(): List<TransactionMessage> =
        voteTransactions + delegateTransactions + transferTransactions + rewardTransaction

    fun getAllStates(): List<StateMessage> = delegateStates + accountStates

    override fun read(buf: ByteBuf) {
        super.read(buf)

        merkleHash = buf.readString()
        stateHash = buf.readString()
        rewardTransaction = RewardTransactionMessage::class.java.newInstance()
        rewardTransaction.read(buf)
        voteTransactions = buf.readList()
        delegateTransactions = buf.readList()
        transferTransactions = buf.readList()
        delegateStates = buf.readList()
        accountStates = buf.readList()
    }

    override fun write(buf: ByteBuf) {
        super.write(buf)

        buf.writeString(merkleHash)
        buf.writeString(stateHash)
        rewardTransaction.write(buf)
        buf.writeList(voteTransactions)
        buf.writeList(delegateTransactions)
        buf.writeList(transferTransactions)
        buf.writeList(delegateStates)
        buf.writeList(accountStates)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseMainBlockMessage

        if (hash != other.hash) return false
        if (timestamp != other.timestamp) return false
        if (signature != other.signature) return false
        if (publicKey != other.publicKey) return false
        if (height != other.height) return false
        if (previousHash != other.previousHash) return false
        if (rewardTransaction != other.rewardTransaction) return false
        if (merkleHash != other.merkleHash) return false
        if (stateHash != other.stateHash) return false
        if (voteTransactions != other.voteTransactions) return false
        if (delegateTransactions != other.delegateTransactions) return false
        if (transferTransactions != other.transferTransactions) return false
        if (delegateStates != other.delegateStates) return false
        if (accountStates != other.accountStates) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hash.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + signature.hashCode()
        result = 31 * result + publicKey.hashCode()
        result = 31 * result + height.hashCode()
        result = 31 * result + previousHash.hashCode()
        result = 31 * result + rewardTransaction.hashCode()
        result = 31 * result + merkleHash.hashCode()
        result = 31 * result + stateHash.hashCode()
        result = 31 * result + voteTransactions.hashCode()
        result = 31 * result + delegateTransactions.hashCode()
        result = 31 * result + transferTransactions.hashCode()
        result = 31 * result + delegateStates.hashCode()
        result = 31 * result + accountStates.hashCode()
        return result
    }

}