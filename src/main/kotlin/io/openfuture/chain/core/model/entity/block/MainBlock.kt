package io.openfuture.chain.core.model.entity.block

import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.domain.NetworkBlock
import io.openfuture.chain.network.domain.NetworkMainBlock
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import javax.persistence.*

@Entity
@Table(name = "main_blocks")
class MainBlock(
    height: Long,
    previousHash: String,
    timestamp: Long,
    reward: Long,
    hash: String,
    publicKey: String,
    signature: String,

    @Column(name = "merkle_hash", nullable = false)
    var merkleHash: String,

    @OneToMany(mappedBy = "block", fetch = FetchType.EAGER)
    var transactions: MutableSet<Transaction> = mutableSetOf()

) : BaseBlock(height, previousHash, timestamp, reward, hash, publicKey, signature) {

    companion object {
        fun of(dto: NetworkMainBlock) : MainBlock = MainBlock(
            dto.height,
            dto.previousHash,
            dto.timestamp,
            dto.reward,
            dto.hash!!,
            dto.publicKey!!,
            dto.signature!!,
            dto.merkleHash
        )
    }

    override fun toMessage(): NetworkMainBlock = NetworkMainBlock(
        height,
        previousHash,
        timestamp,
        reward,
        hash,
        publicKey,
        signature,
        merkleHash,
        transactions.map { it.toMessage() }.toMutableSet()
    )

}