package io.openfuture.chain.domain.vote

import io.openfuture.chain.domain.delegate.DelegateNetworkInfo
import io.openfuture.chain.entity.dictionary.VoteType

data class VoteDto(
    val voteType: VoteType,
    val stakeholderKey: String,
    val delegateInfo: DelegateNetworkInfo
)