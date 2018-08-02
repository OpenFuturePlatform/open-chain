package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.consensus.annotation.NoArgConstructor
import io.openfuture.chain.core.model.dto.transaction.DelegateTransactionDto
import io.openfuture.chain.core.model.dto.transaction.TransferTransactionDto
import io.openfuture.chain.core.model.dto.transaction.VoteTransactionDto
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.network.extension.readList
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeList
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
class NetworkMainBlock : NetworkBlock {
    var merkleHash: String
    var transferTransactions: MutableList<TransferTransactionDto>
    var voteTransactions: MutableList<VoteTransactionDto>
    var delegateTransactions: MutableList<DelegateTransactionDto>

    constructor(height: Long, previousHash: String, blockTimestamp: Long, reward: Long, hash: String, signature: String,
                publicKey: String, merkleHash: String, transferTransactions: MutableList<TransferTransactionDto>,
                voteTransactions: MutableList<VoteTransactionDto>, delegateTransactions: MutableList<DelegateTransactionDto>
    ) : super(height, previousHash, blockTimestamp, reward, publicKey, hash, signature) {
        this.merkleHash = merkleHash
        this.transferTransactions = transferTransactions
        this.voteTransactions = voteTransactions
        this.delegateTransactions = delegateTransactions
    }

    constructor(block: MainBlock) : super(block) {
        merkleHash = block.merkleHash
        transferTransactions = block.transactions.filterIsInstance(TransferTransaction::class.java).map { TransferTransactionDto(it) }.toMutableList(),
        voteTransactions = block.transactions.filterIsInstance(VoteTransaction::class.java).map { VoteTransactionDto(it) }.toMutableList(),
        delegateTransactions = block.transactions.filterIsInstance(DelegateTransaction::class.java).map { DelegateTransactionDto(it) }.toMutableList()
    }

    override fun readParams(buffer: ByteBuf) {
        super.readParams(buffer)

        merkleHash = buffer.readString()
        transferTransactions = buffer.readList()
        voteTransactions = buffer.readList()
        delegateTransactions = buffer.readList()
    }

    override fun writeParams(buffer: ByteBuf) {
        super.writeParams(buffer)

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
