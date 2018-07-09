package io.openfuture.chain.service

import io.openfuture.chain.domain.delegate.AccountDto
import io.openfuture.chain.domain.transaction.data.VoteDto
import io.openfuture.chain.entity.Account
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.exception.ValidationException
import io.openfuture.chain.property.DelegateProperties
import io.openfuture.chain.repository.AccountRepository
import org.apache.commons.collections4.CollectionUtils
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.annotation.PostConstruct

@Service
@Transactional(readOnly = true)
class DefaultAccountService(
        private val repository: AccountRepository,
        private val delegateProperties: DelegateProperties
) : AccountService {

    companion object {
        const val VOTES_LIMIT = 20
    }

    @PostConstruct
    fun initGenesisDelegate() {
        for ((index, publicKey) in delegateProperties.publicKeys!!.withIndex()) {
            val genesisDelegate = Account("genesisDelegate$index", delegateProperties.address!!, publicKey, true)
            repository.save(genesisDelegate)
        }
    }

    override fun getAll(): List<Account> = repository.findAll()

    override fun getAccountByPublicKey(publicKey: String): Account = repository.findOneByPublicKey(publicKey)
            ?: throw NotFoundException("Account with publicKey: $publicKey not exist!")

    override fun getDelegateByPublicKey(publicKey: String): Account = repository.findOneByPublicKeyAndIsDelegateIsTrue(publicKey)
            ?: throw NotFoundException("Delegate with publicKey: $publicKey not exist!")

    override fun getActiveDelegates(): List<Account> {
        val result = mutableListOf<Account>()
        val request = PageRequest.of(0, 21, Sort(Sort.Direction.DESC, "rating"))
        result.addAll(repository.findAll(request).content)
        return result
    }

    override fun isValidActiveDelegates(publicKeysDelegates: List<String>): Boolean {
        val publicKeysActiveDelegates = this.getActiveDelegates()
        return CollectionUtils.containsAny(publicKeysActiveDelegates, publicKeysDelegates)
    }

    @Transactional
    override fun add(dto: AccountDto): Account = repository.save(Account.of(dto))

    @Transactional
    override fun updateRatingByVote(dto: VoteDto) {
        val account = this.getAccountByPublicKey(dto.accountKey)
        val delegate = this.getDelegateByPublicKey(dto.delegateKey)

        if (VOTES_LIMIT <= delegate.votes.size && dto.voteType == VoteType.FOR) {
            throw ValidationException("Account with publicKey ${account.publicKey} already spent all votes!")
        }

        if (account.votes.contains(delegate) && dto.voteType == VoteType.FOR) {
            throw ValidationException("Account with publicKey ${account.publicKey} already voted for delegate with publicKey " +
                    "${delegate.publicKey}!")
        }

        if (!account.votes.contains(delegate) && dto.voteType == VoteType.AGAINST) {
            throw ValidationException("Account with publicKey ${account.publicKey} can't remove vote from delegate with " +
                    "publicKey ${delegate.publicKey}!")
        }

        if (dto.voteType == VoteType.FOR) {
            delegate.rating+= dto.value
            account.votes.add(delegate)
        } else {
            delegate.rating-= dto.value
            account.votes.remove(delegate)
        }

        repository.save(delegate)
        repository.save(account)
    }

}