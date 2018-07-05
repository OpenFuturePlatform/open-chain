package io.openfuture.chain.domain.transaction

import io.openfuture.chain.domain.transaction.vote.VoteTransactionData
import io.openfuture.chain.entity.dictionary.TransactionType

class VoteTransactionDto(
        timestamp: Long,
        hash: String,
        var data: VoteTransactionData
): TransactionDto(timestamp, hash, TransactionType.VOTE)
