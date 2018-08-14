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
    hash: String,
    signature: String,
    publicKey: String,

    @Embedded
    var payload: MainBlockPayload

) : BaseBlock(timestamp, height, previousHash, hash, signature, publicKey) {

    companion object {
        fun of(message: PendingBlockMessage): MainBlock = MainBlock(
            message.timestamp,
            message.height,
            message.previousHash,
            message.hash,
            message.signature,
            message.publicKey,
            MainBlockPayload(message.merkleHash)
        )

        fun of(message: MainBlockMessage): MainBlock = MainBlock(
            message.timestamp,
            message.height,
            message.previousHash,
            message.hash,
            message.signature,
            message.publicKey,
            MainBlockPayload(message.merkleHash)
        )
    }

    override fun getPayload(): BlockPayload = payload

}