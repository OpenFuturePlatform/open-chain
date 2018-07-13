package io.openfuture.chain.service

import io.openfuture.chain.domain.stakeholder.StakeholderDto
import io.openfuture.chain.entity.Stakeholder
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.StakeholderRepository
import io.openfuture.chain.service.StakeholderService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultStakeholderService(
    private val repository: StakeholderRepository
) : StakeholderService{

    @Transactional(readOnly = true)
    override fun getAll(): List<Stakeholder> = repository.findAll()

    @Transactional(readOnly = true)
    override fun getByPublicKey(publicKey: String): Stakeholder =
        repository.findOneByPublicKey(publicKey) ?: throw NotFoundException("Stakeholder with " +
            "publicKey: $publicKey not exist!")

    @Transactional
    override fun add(dto: StakeholderDto): Stakeholder = repository.save(Stakeholder.of(dto))

    @Transactional
    override fun save(entity: Stakeholder): Stakeholder = repository.save(entity)

}