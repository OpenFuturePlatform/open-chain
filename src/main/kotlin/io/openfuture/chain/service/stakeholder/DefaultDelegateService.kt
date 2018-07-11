package io.openfuture.chain.service.stakeholder

import io.openfuture.chain.domain.stakeholder.DelegateDto
import io.openfuture.chain.domain.transaction.data.VoteDto
import io.openfuture.chain.entity.account.Delegate
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.nio.client.handler.ConnectionClientHandler
import io.openfuture.chain.property.DelegateProperties
import io.openfuture.chain.repository.DelegateRepository
import io.openfuture.chain.service.DelegateService
import io.openfuture.chain.service.StakeholderService
import org.apache.commons.collections4.CollectionUtils
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.annotation.PostConstruct

@Service
class DefaultDelegateService(
    private val repository: DelegateRepository,
    private val delegateProperties: DelegateProperties,
    private val stakeholderService: StakeholderService
) : DefaultBaseStakeholderService<Delegate, DelegateDto>(repository), DelegateService {

    companion object {
        private val log = LoggerFactory.getLogger(ConnectionClientHandler::class.java)
        const val VOTES_LIMIT = 20
    }

    @PostConstruct
    fun initGenesisDelegate() {
        for ((index, publicKey) in delegateProperties.publicKeys!!.withIndex()) {
            val genesisDelegate = Delegate("genesisDelegate$index", delegateProperties.address!!, publicKey)
            repository.save(genesisDelegate)
        }
    }

    @Transactional
    override fun add(dto: DelegateDto): Delegate = repository.save(Delegate.of(dto))

    @Transactional(readOnly = true)
    override fun getActiveDelegates(): List<Delegate> {
        val result = mutableListOf<Delegate>()
        val request = PageRequest.of(0, delegateProperties.count!!, Sort(Sort.Direction.DESC, "rating"))
        result.addAll(repository.findAll(request).content)
        return result
    }

    @Transactional(readOnly = true)
    override fun isValidActiveDelegates(publicKeysDelegates: List<String>): Boolean {
        val publicKeysActiveDelegates = this.getActiveDelegates()
        return CollectionUtils.containsAny(publicKeysActiveDelegates, publicKeysDelegates)
    }

    @Transactional
    override fun updateDelegateRatingByVote(dto: VoteDto) {
        val stakeholder = stakeholderService.getByPublicKey(dto.stakeholderKey)
        val delegate = this.getByPublicKey(dto.delegateKey)

        if (VOTES_LIMIT <= delegate.votes.size && dto.voteType == VoteType.FOR) {
            //todo need to throw exception ?
            log.error("Stakeholder with publicKey ${stakeholder.publicKey} already spent all votes!")
            return
        }

        if (stakeholder.votes.contains(delegate) && dto.voteType == VoteType.FOR) {
            //todo need to throw exception ?
            log.error("Stakeholder with publicKey ${stakeholder.publicKey} already voted for stakeholder with publicKey " +
                "${delegate.publicKey}!")
            return
        }

        if (!stakeholder.votes.contains(delegate) && dto.voteType == VoteType.AGAINST) {
            //todo need to throw exception ?
            log.error("Stakeholder with publicKey ${stakeholder.publicKey} can't remove vote from stakeholder with " +
                "publicKey ${delegate.publicKey}!")
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

}