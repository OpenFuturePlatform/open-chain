package io.openfuture.chain.service

import io.openfuture.chain.repository.VoteRepository
import org.springframework.stereotype.Service

@Service
class DefaultVoteService(
        private val voteRepository: VoteRepository
) : VoteService {

    override fun getVotesByPublicKey(publicKey: String) = voteRepository.findAllByPublicKey(publicKey)

}