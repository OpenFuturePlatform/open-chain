package io.openfuture.chain.entity

import io.openfuture.chain.domain.block.MainBlockDto
import javax.persistence.*

@Entity
@Table(name = "main_blocks")
class MainBlock(hash: String, height: Long, previousHash: String,
                merkleHash: String, timestamp: Long,

                @Column(name = "signature", nullable = false)
                var signature: String,

                @OneToMany
                @JoinColumn(name = "block_id", nullable = true)
                var transactions: MutableSet<Transaction> = mutableSetOf()

) : Block(hash, height, previousHash, merkleHash, timestamp, BlockVersion.MAIN.version) {

    fun toDto(): MainBlockDto = MainBlockDto(
            this.hash,
            this.height,
            this.previousHash,
            this.merkleHash,
            this.timestamp,
            this.signature,
            this.transactions.map { it.toDto() }.toSet()
    )

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