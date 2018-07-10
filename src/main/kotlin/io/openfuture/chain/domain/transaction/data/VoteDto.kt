package io.openfuture.chain.domain.transaction.data

import io.openfuture.chain.entity.dictionary.VoteType

data class VoteDto(
        val stakeholderKey: String,
        val delegateKey: String,
        val voteType: VoteType,
        val value: Int
)