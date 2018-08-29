package io.openfuture.chain.core.model.domain.block

import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction

data class BlockTransactionsResponse(
    val voteTransactions: List<UnconfirmedVoteTransaction>,
    val delegateTransactions: List<UnconfirmedDelegateTransaction>,
    val transferTransactions: List<UnconfirmedTransferTransaction>
) {

    fun getAll(): List<UnconfirmedTransaction> = voteTransactions + delegateTransactions + transferTransactions

}