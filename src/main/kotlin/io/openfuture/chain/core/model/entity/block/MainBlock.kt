package io.openfuture.chain.core.model.entity.block

import io.openfuture.chain.core.model.entity.block.payload.BlockPayload
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
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

) : BaseBlock(timestamp, height, previousHash, reward, hash, signature, publicKey) {

    companion object {
        fun of(dto: MainBlockMessage): MainBlock = MainBlock(
            dto.timestamp,
            dto.height,
            dto.previousHash,
            dto.reward,
            dto.hash,
            dto.signature,
            dto.publicKey,
            MainBlockPayload(dto.merkleHash)
        )
    }

    override fun getPayload(): BlockPayload {
        return payload
    }

}