package io.openfuture.chain.core.model.entity.block

import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.payload.BlockPayload
import io.openfuture.chain.core.model.entity.block.payload.GenesisBlockPayload
import io.openfuture.chain.network.message.core.GenesisBlockMessage
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "genesis_blocks")
class GenesisBlock(
    timestamp: Long,
    height: Long,
    previousHash: String,
    hash: String,
    signature: String,
    publicKey: String,

    @Embedded
    val payload: GenesisBlockPayload

) : Block(timestamp, height, previousHash, hash, signature, publicKey) {


    companion object {
        fun of(dto: GenesisBlockMessage, delegates: List<Delegate>): GenesisBlock = GenesisBlock(
            dto.timestamp,
            dto.height,
            dto.previousHash,
            dto.hash,
            dto.signature,
            dto.publicKey,
            GenesisBlockPayload(dto.epochIndex, delegates)
        )
    }

    override fun getPayload(): BlockPayload = payload

    override fun toMessage(): GenesisBlockMessage = GenesisBlockMessage (
        height,
        previousHash,
        timestamp,
        hash,
        signature,
        publicKey,
        payload.epochIndex,
        payload.activeDelegates.map { it.publicKey }
    )

}