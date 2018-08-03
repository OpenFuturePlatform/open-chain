package io.openfuture.chain.core.model.entity.block

import io.openfuture.chain.core.model.entity.block.payload.GenesisBlockPayload
import io.openfuture.chain.network.message.application.block.GenesisBlockMessage
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "genesis_blocks")
class GenesisBlock(
    timestamp: Long,
    height: Long,
    payload: GenesisBlockPayload,
    hash: String,
    signature: String,
    publicKey: String

) : BaseBlock<GenesisBlockPayload>(timestamp, height, payload, hash, signature, publicKey) {

    companion object {
        fun of(dto: GenesisBlockMessage): GenesisBlock = GenesisBlock(
            dto.timestamp,
            dto.height,
            GenesisBlockPayload(dto.previousHash, dto.reward, dto.epochIndex),
            dto.hash,
            dto.signature,
            dto.publicKey
        )
    }

//    fun sign(publicKey: String, privateKey: ByteArray): GenesisBlock {
//        this.publicKey = publicKey
//        this.hash = ByteUtils.toHexString(HashUtils.doubleSha256((getBytes())))
//        this.signature = SignatureUtils.sign(getBytes(), privateKey)
//        return this
//    }

}