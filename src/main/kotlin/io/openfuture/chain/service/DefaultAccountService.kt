package io.openfuture.chain.service

import io.openfuture.chain.domain.delegate.AccountDto
import io.openfuture.chain.domain.transaction.data.VoteDto
import io.openfuture.chain.entity.account.Account
import io.openfuture.chain.entity.account.Delegate
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.nio.client.handler.ConnectionClientHandler
import io.openfuture.chain.property.DelegateProperties
import io.openfuture.chain.repository.AccountRepository
import io.openfuture.chain.repository.DelegateRepository
import org.apache.commons.collections4.CollectionUtils
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.annotation.PostConstruct

@Service
class DefaultAccountService(
        private val repository: AccountRepository<Account>,
        private val delegateRepository: DelegateRepository,
        private val delegateProperties: DelegateProperties
) : AccountService {

    companion object {
        private val log = LoggerFactory.getLogger(ConnectionClientHandler::class.java)
        const val VOTES_LIMIT = 20
    }

    @PostConstruct
    fun initGenesisDelegate() {
        for ((index, publicKey) in delegateProperties.publicKeys!!.withIndex()) {
            val genesisDelegate = Delegate("genesisDelegate$index", delegateProperties.address!!, publicKey)
            delegateRepository.save(genesisDelegate)
        }
    }

    @Transactional(readOnly = true)
    override fun getAllAccounts(): List<Account> = repository.findAll()

    @Transactional(readOnly = true)
    override fun getAccountByPublicKey(publicKey: String): Account = repository.findOneByPublicKey(publicKey)
            ?: throw NotFoundException("Account with publicKey: $publicKey not exist!")

    @Transactional
    override fun addAccount(dto: AccountDto): Account = repository.save(Account.of(dto))

    @Transactional(readOnly = true)
    override fun getAllDelegates(): List<Delegate> = delegateRepository.findAll()

    @Transactional(readOnly = true)
    override fun getDelegateByPublicKey(publicKey: String): Delegate = delegateRepository.findOneByPublicKey(publicKey)
            ?: throw NotFoundException("Delegate with publicKey: $publicKey not exist!")

    @Transactional(readOnly = true)
    override fun getActiveDelegates(): List<Delegate> {
        val result = mutableListOf<Delegate>()
        val request = PageRequest.of(0, 21, Sort(Sort.Direction.DESC, "rating"))
        result.addAll(delegateRepository.findAll(request).content)
        return result
    }

    @Transactional(readOnly = true)
    override fun isValidActiveDelegates(publicKeysDelegates: List<String>): Boolean {
        val publicKeysActiveDelegates = this.getActiveDelegates()
        return CollectionUtils.containsAny(publicKeysActiveDelegates, publicKeysDelegates)
    }

    @Transactional
    override fun updateDelegateRatingByVote(dto: VoteDto) {
        val account = this.getAccountByPublicKey(dto.accountKey)
        val delegate = this.getDelegateByPublicKey(dto.delegateKey)

        if (VOTES_LIMIT <= delegate.votes.size && dto.voteType == VoteType.FOR) {
            //todo need to throw exception ?
            log.error("Account with publicKey ${account.publicKey} already spent all votes!")
        }

        if (account.votes.contains(delegate) && dto.voteType == VoteType.FOR) {
            //todo need to throw exception ?
            log.error("Account with publicKey ${account.publicKey} already voted for delegate with publicKey " +
                    "${delegate.publicKey}!")
        }

        if (!account.votes.contains(delegate) && dto.voteType == VoteType.AGAINST) {
            //todo need to throw exception ?
            log.error("Account with publicKey ${account.publicKey} can't remove vote from delegate with " +
                    "publicKey ${delegate.publicKey}!")
        }

        if (dto.voteType == VoteType.FOR) {
            delegate.rating+= dto.value
            account.votes.add(delegate)
        } else {
            delegate.rating-= dto.value
            account.votes.remove(delegate)
        }

        repository.save(account)
        delegateRepository.save(delegate)
    }

}