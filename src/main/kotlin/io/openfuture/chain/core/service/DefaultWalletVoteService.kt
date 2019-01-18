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
    override fun getVotesForNode(nodeId: String): List<WalletVote> = repository.findAllByIdNodeId(nodeId)

    @Transactional
    override fun updateVoteByAddress(address: String, nodeId: String, type: VoteType) {
        when (type) {
            VoteType.FOR -> repository.save(WalletVote(address, nodeId))
            VoteType.AGAINST -> repository.deleteByIdAddressAndIdNodeId(address, nodeId)
        }
    }

}