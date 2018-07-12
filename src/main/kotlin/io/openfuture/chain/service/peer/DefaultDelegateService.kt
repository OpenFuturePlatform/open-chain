package io.openfuture.chain.service.peer

import io.openfuture.chain.domain.node.DelegateDto
import io.openfuture.chain.domain.vote.VoteDto
import io.openfuture.chain.entity.peer.Delegate
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.nio.client.handler.ConnectionClientHandler
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.repository.DelegateRepository
import io.openfuture.chain.service.DelegateService
import io.openfuture.chain.service.StakeholderService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.CollectionUtils

@Service
class DefaultDelegateService(
    repository: DelegateRepository,
    private val consensusProperties: ConsensusProperties,
    private val stakeholderService: StakeholderService
) : DefaultBasePeerService<Delegate, DelegateDto>(repository), DelegateService {

    companion object {
        private val log = LoggerFactory.getLogger(ConnectionClientHandler::class.java)
        private const val VOTES_LIMIT = 21
        private const val RATING = "rating"
    }


    @Transactional
    override fun add(dto: DelegateDto): Delegate = repository.save(Delegate.of(dto))

    @Transactional(readOnly = true)
    override fun getActiveDelegates(): List<Delegate> {
        val request = PageRequest.of(0, consensusProperties.delegatesCount!!, Sort(Sort.Direction.DESC, RATING))
        return repository.findAll(request).content
    }

    @Transactional(readOnly = true)
    override fun isValidActiveDelegates(publicKeysDelegates: List<String>): Boolean {
        val publicKeysActiveDelegates = this.getActiveDelegates()
        return CollectionUtils.containsAny(publicKeysActiveDelegates, publicKeysDelegates)
    }

    @Transactional
    override fun updateDelegateRatingByVote(dto: VoteDto) {
        val stakeholder = stakeholderService.getByPublicKey(dto.stakeholderKey)
        val delegate = this.getByNetworkId(dto.networkId)

        if (VOTES_LIMIT <= stakeholder.votes.size && dto.voteType == VoteType.FOR) {
            //todo need to throw exception ?
            log.error("Stakeholder with publicKey ${stakeholder.publicKey} already spent all votes!")
            return
        }

        if (stakeholder.votes.contains(delegate) && dto.voteType == VoteType.FOR) {
            //todo need to throw exception ?
            log.error("Stakeholder with publicKey ${stakeholder.publicKey} already voted for " +
                "delegate with network_id ${delegate.networkId}!")
            return
        }

        if (!stakeholder.votes.contains(delegate) && dto.voteType == VoteType.AGAINST) {
            //todo need to throw exception ?
            log.error("Stakeholder with publicKey ${stakeholder.publicKey} can't remove vote from " +
                "delegate with network_id ${delegate.networkId}!")
            return
        }

        if (dto.voteType == VoteType.FOR) {
            delegate.rating += dto.value
            stakeholder.votes.add(delegate)
        } else {
            delegate.rating -= dto.value
            stakeholder.votes.remove(delegate)
        }

        stakeholderService.save(stakeholder)
        repository.save(delegate)
    }

    override fun addAll(list: List<DelegateDto>) {
        val delegates = list.map { Delegate.of(it) }
        repository.saveAll(delegates)
    }

}