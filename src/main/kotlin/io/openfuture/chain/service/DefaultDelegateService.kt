package io.openfuture.chain.service

import io.openfuture.chain.domain.delegate.DelegateDto
import io.openfuture.chain.entity.Delegate
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.repository.DelegateRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultDelegateService(
    private val repository: DelegateRepository,
    private val consensusProperties: ConsensusProperties
) : DelegateService {

    @Transactional(readOnly = true)
    override fun getByHostAndPort(host: String, port: Int): Delegate = repository.findOneByHostAndPort(host, port)
        ?: throw NotFoundException("Delegate with host: $host and port $port not exist!")

    @Transactional(readOnly = true)
    override fun getActiveDelegates(): Set<Delegate> {
        val request = PageRequest.of(0, consensusProperties.delegatesCount!!)
        return repository.findAllByOrderByRatingDesc(request).toSet()
    }

    @Transactional
    override fun add(dto: DelegateDto): Delegate = repository.save(Delegate.of(dto))

    @Transactional
    override fun save(delegate: Delegate): Delegate = repository.save(delegate)

}