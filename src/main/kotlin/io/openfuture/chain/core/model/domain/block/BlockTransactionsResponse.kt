package io.openfuture.chain.core.model.domain.block

import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction

data class BlockTransactionsResponse(
    private val votesTx: List<VoteTransaction>,
    private val delegatesTx: List<DelegateTransaction>,
    private val transferTx: List<TransferTransaction>
)