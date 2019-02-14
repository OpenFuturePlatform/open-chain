package io.openfuture.chain.core.service.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.dictionary.VoteType.AGAINST
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.repository.UVoteTransactionRepository
import io.openfuture.chain.core.service.UVoteTransactionService
import io.openfuture.chain.core.sync.BlockchainLock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultUVoteTransactionService(
    private val uRepository: UVoteTransactionRepository
) : DefaultUTransactionService<UnconfirmedVoteTransaction>(uRepository), UVoteTransactionService {

    override fun getBySenderAgainstDelegate(senderAddress: String, delegateKey: String): UnconfirmedVoteTransaction? {
        BlockchainLock.readLock.lock()
        try {
            return uRepository.findOneBySenderAddressAndPayloadDelegateKeyAndPayloadVoteTypeId(senderAddress, delegateKey, AGAINST.getId())
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

}