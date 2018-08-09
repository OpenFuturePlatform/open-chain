package io.openfuture.chain.core.model.entity.block

import io.openfuture.chain.core.model.entity.block.payload.BlockPayload
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.MainBlockMessage
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "main_blocks")
class MainBlock(
    timestamp: Long,
    height: Long,
    previousHash: String,
    reward: Long,
    hash: String,
    signature: String,
    publicKey: String,

    @Embedded
    var payload: MainBlockPayload

) : Block(timestamp, height, previousHash, reward, hash, signature, publicKey) {

    companion object {
        fun of(message: PendingBlockMessage): MainBlock = MainBlock(
            message.timestamp,
            message.height,
            message.previousHash,
            message.reward,
            message.hash,
            message.signature,
            message.publicKey,
            MainBlockPayload(message.merkleHash)
        )

        fun of(message: MainBlockMessage): MainBlock = MainBlock(
            message.timestamp,
            message.height,
            message.previousHash,
            message.reward,
            message.hash,
            message.signature,
            message.publicKey,
            MainBlockPayload(message.merkleHash)
        )
    }

    override fun getPayload(): BlockPayload = payload

    override fun toMessage(): MainBlockMessage = MainBlockMessage(
        height,
        previousHash,
        timestamp,
        reward,
        hash,
        signature,
        publicKey,
        payload.merkleHash,
        payload.voteTransactions.map { it.toMessage() },
        payload.delegateTransactions.map { it.toMessage() },
        payload.transferTransactions.map { it.toMessage() }
    )

}