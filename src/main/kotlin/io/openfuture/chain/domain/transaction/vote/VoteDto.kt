package io.openfuture.chain.domain.transaction.vote

import io.openfuture.chain.entity.Vote

data class VoteDto(
        var pubicKey: String,
        var weight: Int
) {

    companion object {
        fun of(vote: Vote): VoteDto = VoteDto(
                vote.publicKey,
                vote.weight
        )
    }

}
