package io.openfuture.chain.core.model.entity.block

import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.model.entity.state.AccountState
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.network.message.core.BaseMainBlockMessage
import io.openfuture.chain.network.message.sync.MainBlockMessage
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "main_blocks")
class MainBlock(
    timestamp: Long,
    height: Long,
    previousHash: String,
    hash: String,
    signature: String,
    publicKey: String,

    @Embedded
    private var payload: MainBlockPayload

) : Block(timestamp, height, previousHash, hash, signature, publicKey) {

    companion object {
        fun of(message: BaseMainBlockMessage): MainBlock = MainBlock(
            message.timestamp,
            message.height,
            message.previousHash,
            message.hash,
            message.signature,
            message.publicKey,
            MainBlockPayload(
                message.transactionMerkleHash,
                message.stateMerkleHash,
                message.receiptMerkleHash,
                message.rewardTransactions.map { RewardTransaction.of(it) },
                message.voteTransactions.map { VoteTransaction.of(it) },
                message.delegateTransactions.map { DelegateTransaction.of(it) },
                message.transferTransactions.map { TransferTransaction.of(it) },
                message.delegateStates.map { DelegateState.of(it) },
                message.accountStates.map { AccountState.of(it) },
                message.receipts.map { Receipt.of(it) }
            )
        )
    }


    override fun toMessage(): MainBlockMessage = MainBlockMessage(
        height,
        previousHash,
        timestamp,
        hash,
        signature,
        publicKey,
        payload.transactionMerkleHash,
        payload.stateMerkleHash,
        payload.receiptMerkleHash,
        payload.rewardTransactions.map { it.toMessage() },
        payload.voteTransactions.map { it.toMessage() },
        payload.delegateTransactions.map { it.toMessage() },
        payload.transferTransactions.map { it.toMessage() },
        payload.delegateStates.map { it.toMessage() },
        payload.accountStates.map { it.toMessage() },
        payload.receipts.map { it.toMessage() }
    )

    override fun getPayload(): MainBlockPayload = payload

}