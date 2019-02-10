package io.openfuture.chain.core.service.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.dictionary.VoteType.AGAINST
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.repository.UVoteTransactionRepository
import io.openfuture.chain.core.service.UVoteTransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultUVoteTransactionService(
    private val uRepository: UVoteTransactionRepository
) : DefaultUTransactionService<UnconfirmedVoteTransaction, UVoteTransactionRepository>(uRepository),
    UVoteTransactionService {

    override fun getUnconfirmedBySenderAgainstDelegate(senderAddress: String, delegateKey: String): UnconfirmedVoteTransaction? =
        uRepository.findOneBySenderAddressAndPayloadDelegateKeyAndPayloadVoteTypeId(senderAddress, delegateKey, AGAINST.getId())

}