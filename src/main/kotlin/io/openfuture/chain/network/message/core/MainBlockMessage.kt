package io.openfuture.chain.network.message.application.block

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readList
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeList
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.message.application.transaction.DelegateTransactionMessage
import io.openfuture.chain.network.message.application.transaction.TransferTransactionMessage
import io.openfuture.chain.network.message.application.transaction.VoteTransactionMessage
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils

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
    var transferTransactions: MutableList<TransferTransactionMessage>,
    var voteTransactions: MutableList<VoteTransactionMessage>,
    var delegateTransactions: MutableList<DelegateTransactionMessage>
) : BlockMessage(height, previousHash, timestamp, reward, hash, signature, publicKey) {

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
