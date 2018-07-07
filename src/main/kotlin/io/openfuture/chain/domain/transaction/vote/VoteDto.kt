package io.openfuture.chain.domain.transaction.vote

import io.openfuture.chain.entity.Vote
import io.openfuture.chain.entity.VoteTransaction

data class VoteDto(
        var pubicKey: String,
        var weight: Int
) {
    fun toEntity(transaction: VoteTransaction): Vote = Vote(
            transaction,
            pubicKey,
            weight
    )

}
