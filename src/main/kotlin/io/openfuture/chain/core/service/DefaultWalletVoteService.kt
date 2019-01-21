package io.openfuture.chain.core.service

import io.openfuture.chain.core.model.entity.WalletVote
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.repository.WalletVoteRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultWalletVoteService(
    private val repository: WalletVoteRepository
) : WalletVoteService {

    @Transactional(readOnly = true)
    override fun getVotesByAddress(address: String): List<WalletVote> = repository.findAllByIdAddress(address)

    @Transactional(readOnly = true)
    override fun getVotesForDelegate(delegateKey: String): List<WalletVote> = repository.findAllByIdDelegateKey(delegateKey)

    @Transactional
    override fun updateVoteByAddress(address: String, delegateKey: String, type: VoteType) {
        when (type) {
            VoteType.FOR -> repository.save(WalletVote(address, delegateKey))
            VoteType.AGAINST -> repository.deleteByIdAddressAndIdDelegateKey(address, delegateKey)
        }
    }

}