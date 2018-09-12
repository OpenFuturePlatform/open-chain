package io.openfuture.chain.core.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.Delegate
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
    override fun getByPublicKey(key: String): Delegate = repository.findOneByPublicKey(key)
        ?: throw NotFoundException("Delegate with key: $key not exist!")

    @Transactional(readOnly = true)
    override fun getByNodeId(nodeId: String): Delegate = repository.findOneByNodeId(nodeId)
        ?: throw NotFoundException("Delegate with nodeId: $nodeId not exist!")

    @Transactional(readOnly = true)
    override fun getActiveDelegates(): List<Delegate> {
        val sortFields = arrayOf("rating", "registrationDate", "id")
        val pageRequest = PageRequest(0, consensusProperties.delegatesCount!!, sortFields, Sort.Direction.DESC)
        val a = viewRepository.findAll(pageRequest)
            .map { Delegate(it.publicKey, it.nodeId, it.address, it.host, it.port, it.registrationDate, it.id) }
            .toList()
        System.err.println(jacksonObjectMapper().writeValueAsString(a))
        return a
    }

    @Transactional(readOnly = true)
    override fun isExistsByPublicKey(key: String): Boolean = repository.existsByPublicKey(key)

    @Transactional(readOnly = true)
    override fun isExistsByNodeId(nodeId: String): Boolean = repository.existsByNodeId(nodeId)

    @Transactional
    override fun save(delegate: Delegate): Delegate = repository.save(delegate)

}