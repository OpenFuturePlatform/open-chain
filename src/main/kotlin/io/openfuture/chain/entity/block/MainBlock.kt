package io.openfuture.chain.entity.block

import io.openfuture.chain.domain.block.MainBlockDto
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.entity.dictionary.BlockVersion
import javax.persistence.*

@Entity
@Table(name = "main_blocks")
class MainBlock(
    hash: String,
    height: Long,
    previousHash: String,
    merkleHash: String, timestamp: Long,

    @Column(name = "signature", nullable = false)
    var signature: String,

    @OneToMany
    @JoinColumn(name = "block_id", nullable = true)
    var transactions: MutableSet<BaseTransaction> = mutableSetOf()

) : Block(hash, height, previousHash, merkleHash, timestamp, BlockVersion.MAIN.version) {

    companion object {
        fun of(dto: MainBlockDto): MainBlock = MainBlock(
            dto.hash,
            dto.height,
            dto.previousHash,
            dto.merkleHash,
            dto.timestamp,
            dto.signature)
    }

}