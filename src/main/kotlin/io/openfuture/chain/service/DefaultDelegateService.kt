package io.openfuture.chain.service

import io.openfuture.chain.domain.delegate.DelegateDto
import io.openfuture.chain.entity.Delegate
import io.openfuture.chain.exception.NotFoundException
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
        private val transactionService: TransactionService,
        private val delegateProperties: DelegateProperties
) : DelegateService {

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
        val publicKeysActiveDelegates = listOf<String>() // TODO("getting of public keys top 21 delegates from any vote service")
        val activeDelegates = mutableListOf<Delegate>()
        publicKeysActiveDelegates.forEach { activeDelegates.add(getByPublicKey(it)) }

        return activeDelegates
    }

    override fun isValidActiveDelegates(publicKeysDelegates: List<String>): Boolean {
        val publicKeysActiveDelegates = listOf<String>() // TODO("getting of public keys top 21 delegates from any vote service")

        return CollectionUtils.containsAny(publicKeysActiveDelegates, publicKeysDelegates)
    }

    @Transactional
    override fun add(dto: DelegateDto): Delegate = repository.save(Delegate.of(dto))

}