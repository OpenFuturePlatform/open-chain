package io.openfuture.chain.service

import io.openfuture.chain.domain.delegate.DelegateDto
import io.openfuture.chain.domain.transaction.data.VoteDto
import io.openfuture.chain.entity.Delegate
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.exception.ValidationException
import io.openfuture.chain.property.DelegateProperties
import io.openfuture.chain.repository.DelegateRepository
import org.apache.commons.collections4.CollectionUtils
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.annotation.PostConstruct

@Service
@Transactional(readOnly = true)
class DefaultDelegateService(
        private val repository: DelegateRepository,
        private val delegateProperties: DelegateProperties
) : DelegateService {

    companion object {
        const val VOTES_LIMIT = 20
    }

    @PostConstruct
    fun initGenesisDelegate() {
        for ((index, publicKey) in delegateProperties.publicKeys!!.withIndex()) {
            val genesisDelegate = Delegate("genesisDelegate$index", delegateProperties.address!!, publicKey)
            repository.save(genesisDelegate)
        }
    }

    override fun getAll(): List<Delegate> = repository.findAll()

    override fun getByPublicKey(publicKey: String): Delegate = repository.findOneByPublicKey(publicKey)
            ?: throw NotFoundException("Delegate with such publicKey: $publicKey not exist!")

    override fun getActiveDelegates(): List<Delegate> {
        val result = mutableListOf<Delegate>()
        val request = PageRequest.of(0, 21, Sort(Sort.Direction.DESC, "rating"))
        result.addAll(repository.findAll(request).content)
        return result
    }

    override fun isValidActiveDelegates(publicKeysDelegates: List<String>): Boolean {
        val publicKeysActiveDelegates = this.getActiveDelegates()
        return CollectionUtils.containsAny(publicKeysActiveDelegates, publicKeysDelegates)
    }

    @Transactional
    override fun add(dto: DelegateDto): Delegate = repository.save(Delegate.of(dto))

    @Transactional
    override fun updateRatingByVote(dto: VoteDto) {
        val sender = this.getByPublicKey(dto.senderKey)
        val delegate = this.getByPublicKey(dto.delegateKey)

        if (VOTES_LIMIT <= delegate.votes.size && dto.voteType == VoteType.FOR) {
            throw ValidationException("Delegate with key ${delegate.publicKey} already spent all votes!")
        }

        if (sender.votes.contains(delegate) && dto.voteType == VoteType.FOR) {
            throw ValidationException("Sender with key ${sender.publicKey} already voted for delegate with publicKey " +
                    "${delegate.publicKey}!")
        }

        if (!sender.votes.contains(delegate) && dto.voteType == VoteType.AGAINST) {
            throw ValidationException("Sender with key ${sender.publicKey} can't remove vote for delegate with " +
                    "publicKey ${delegate.publicKey}!")
        }

        if (dto.voteType == VoteType.FOR) {
            delegate.rating+= dto.value
            sender.votes.add(delegate)
        } else {
            delegate.rating-= dto.value
            sender.votes.remove(delegate)
        }

        repository.save(delegate)
        repository.save(sender)
    }

}