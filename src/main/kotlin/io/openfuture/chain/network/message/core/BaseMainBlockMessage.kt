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
    var transactionMerkleHash: String,
    var stateMerkleHash: String,
    var receiptMerkleHash: String,
    var rewardTransactions: List<RewardTransactionMessage>,
    var voteTransactions: List<VoteTransactionMessage>,
    var delegateTransactions: List<DelegateTransactionMessage>,
    var transferTransactions: List<TransferTransactionMessage>,
    var delegateStates: List<DelegateStateMessage>,
    var accountStates: List<AccountStateMessage>,
    var receipts: List<ReceiptMessage>
) : BlockMessage(height, previousHash, timestamp, hash, signature, publicKey) {

    fun getAllTransactions(): List<TransactionMessage> =
        voteTransactions + delegateTransactions + transferTransactions + rewardTransactions

    fun getAllStates(): List<StateMessage> = delegateStates + accountStates

    override fun read(buf: ByteBuf) {
        super.read(buf)

        transactionMerkleHash = buf.readString()
        stateMerkleHash = buf.readString()
        receiptMerkleHash = buf.readString()
        rewardTransactions = buf.readList()
        voteTransactions = buf.readList()
        delegateTransactions = buf.readList()
        transferTransactions = buf.readList()
        delegateStates = buf.readList()
        accountStates = buf.readList()
        receipts = buf.readList()
    }

    override fun write(buf: ByteBuf) {
        super.write(buf)

        buf.writeString(transactionMerkleHash)
        buf.writeString(stateMerkleHash)
        buf.writeString(receiptMerkleHash)
        buf.writeList(rewardTransactions)
        buf.writeList(voteTransactions)
        buf.writeList(delegateTransactions)
        buf.writeList(transferTransactions)
        buf.writeList(delegateStates)
        buf.writeList(accountStates)
        buf.writeList(receipts)
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
        if (rewardTransactions != other.rewardTransactions) return false
        if (transactionMerkleHash != other.transactionMerkleHash) return false
        if (stateMerkleHash != other.stateMerkleHash) return false
        if (receiptMerkleHash != other.receiptMerkleHash) return false
        if (voteTransactions != other.voteTransactions) return false
        if (delegateTransactions != other.delegateTransactions) return false
        if (transferTransactions != other.transferTransactions) return false
        if (delegateStates != other.delegateStates) return false
        if (accountStates != other.accountStates) return false
        if (receipts != other.receipts) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hash.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + signature.hashCode()
        result = 31 * result + publicKey.hashCode()
        result = 31 * result + height.hashCode()
        result = 31 * result + previousHash.hashCode()
        result = 31 * result + rewardTransactions.hashCode()
        result = 31 * result + transactionMerkleHash.hashCode()
        result = 31 * result + stateMerkleHash.hashCode()
        result = 31 * result + receiptMerkleHash.hashCode()
        result = 31 * result + voteTransactions.hashCode()
        result = 31 * result + delegateTransactions.hashCode()
        result = 31 * result + transferTransactions.hashCode()
        result = 31 * result + delegateStates.hashCode()
        result = 31 * result + accountStates.hashCode()
        result = 31 * result + receipts.hashCode()
        return result
    }

}