package io.openfuture.chain.entity

import io.openfuture.chain.entity.dictionary.TransactionType
import javax.persistence.*

@Entity
@Table(name = "transactions")
class VoteTransaction (
        block: Block,
        timestamp: Long,
        hash: String,
        nodePublicKey: String,
        nodeSignature: String,

        @OneToMany(mappedBy = "transaction", fetch = FetchType.EAGER)
        var votes: List<Vote> = listOf()

): Transaction(block, timestamp, hash, nodePublicKey, nodeSignature, TransactionType.VOTE.getId())