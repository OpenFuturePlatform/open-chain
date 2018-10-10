package io.openfuture.chain.core.service

import io.openfuture.chain.core.model.entity.WalletVote
import io.openfuture.chain.core.repository.WalletVoteRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultWalletVoteService(
    private val repository: WalletVoteRepository
) : WalletVoteService {

    @Transactional(readOnly = true)
    override fun getVotesByAddress(address: String): List<WalletVote> = repository.findAllByIdAddress(address)

    @Transactional
    override fun add(address: String, nodeId: String): WalletVote = repository.save(WalletVote(address, nodeId))

    @Transactional
    override fun remove(address: String, nodeId: String) {
        repository.deleteByIdAddressAndIdNodeId(address, nodeId)
    }

}