package io.openfuture.chain.core.model.entity.block

import io.openfuture.chain.core.model.entity.block.payload.BaseBlockPayload
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
    hash: String,
    signature: String,
    publicKey: String,

    @Embedded
    private var payload: MainBlockPayload

) : BaseBlock(timestamp, height, hash, signature, publicKey) {

    companion object {
        fun of(dto: MainBlockMessage): MainBlock = MainBlock(
            dto.timestamp,
            dto.height,
            dto.hash,
            dto.signature,
            dto.publicKey,
            MainBlockPayload(dto.previousHash, dto.reward, dto.merkleHash)
        )
    }

    override fun getPayload(): MainBlockPayload {
        return payload
    }

}