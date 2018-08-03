package io.openfuture.chain.core.model.entity.block

import io.openfuture.chain.core.model.entity.block.payload.GenesisBlockPayload
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.message.application.block.MainBlockMessage
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "main_blocks")
class MainBlock(
    timestamp: Long,
    height: Long,
    payload: MainBlockPayload,
    hash: String,
    signature: String,
    publicKey: String

) : BaseBlock<MainBlockPayload>(timestamp, height, payload, hash, signature, publicKey) {

    companion object {
        fun of(dto: MainBlockMessage) : MainBlock = MainBlock(
            dto.timestamp,
            dto.height,
            MainBlockPayload(dto.previousHash, dto.reward, dto.merkleHash),
            dto.hash,
            dto.signature,
            dto.publicKey
        )
    }

//    fun sign() : MainBlock {
//        this.publicKey = publicKey
//        this.hash = ByteUtils.toHexString(HashUtils.doubleSha256((getBytes())))
//        this.signature = SignatureUtils.sign(getBytes(), privateKey)
//        return this
//    }



}