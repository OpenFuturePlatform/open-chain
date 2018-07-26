package io.openfuture.chain.service.transaction

import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.entity.transaction.unconfirmed.UVoteTransaction
import io.openfuture.chain.repository.UVoteTransactionRepository
import io.openfuture.chain.repository.VoteTransactionRepository
import io.openfuture.chain.service.VoteTransactionService
import org.springframework.stereotype.Service

@Service
class DefaultVoteTransactionService(
    repository: VoteTransactionRepository,
    uRepository: UVoteTransactionRepository
) : DefaultTransactionService<VoteTransaction, UVoteTransaction>(repository, uRepository),
    VoteTransactionService