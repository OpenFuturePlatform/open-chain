package io.openfuture.chain.core.model.entity.block

import io.openfuture.chain.core.model.entity.block.payload.BaseBlockPayload
import io.openfuture.chain.core.model.entity.block.payload.GenesisBlockPayload
import io.openfuture.chain.network.message.core.GenesisBlockMessage
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "genesis_blocks")
class GenesisBlock(
    timestamp: Long,
    height: Long,
    hash: String,
    signature: String,
    publicKey: String,

    @Embedded
    private var payload: GenesisBlockPayload

) : BaseBlock(timestamp, height, hash, signature, publicKey) {

    companion object {
        fun of(dto: GenesisBlockMessage): GenesisBlock = GenesisBlock(
            dto.timestamp,
            dto.height,
            dto.hash,
            dto.signature,
            dto.publicKey,
            GenesisBlockPayload(dto.previousHash, dto.reward, dto.epochIndex)
        )
    }

    override fun getPayload(): GenesisBlockPayload {
        return payload
    }

}