package io.openfuture.chain.core.service

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.delegate.ViewDelegate
import io.openfuture.chain.core.repository.DelegateRepository
import io.openfuture.chain.core.repository.ViewDelegateRepository
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultDelegateService(
    private val repository: DelegateRepository,
    private val viewRepository: ViewDelegateRepository,
    private val consensusProperties: ConsensusProperties
) : DelegateService {

    @Transactional(readOnly = true)
    override fun getAll(request: PageRequest): Page<Delegate> = repository.findAll(request)

    @Transactional(readOnly = true)
    override fun getAllViews(request: PageRequest): Page<ViewDelegate> = viewRepository.findAll(request)

    @Transactional(readOnly = true)
    override fun getByPublicKey(key: String): Delegate = repository.findOneByPublicKey(key)
        ?: throw NotFoundException("Delegate with key: $key not exist!")

    @Transactional(readOnly = true)
    override fun getActiveDelegates(): List<Delegate> {
        val sortFields = arrayOf("rating", "registrationDate")
        val pageRequest = PageRequest(0, consensusProperties.delegatesCount!!, sortFields, Sort.Direction.DESC)
        return viewRepository.findAll(pageRequest)
            .map { Delegate(it.publicKey, it.address, it.host, it.port, it.registrationDate, it.id) }
            .toList()
    }

    @Transactional(readOnly = true)
    override fun isExistsByPublicKey(key: String): Boolean = repository.findOneByPublicKey(key)?.let { true } ?: false

    @Transactional
    override fun save(delegate: Delegate): Delegate = repository.save(delegate)

}