package io.openfuture.chain.service.stakeholder

import io.openfuture.chain.domain.stakeholder.StakeholderDto
import io.openfuture.chain.entity.account.Stakeholder
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.StakeholderRepository
import io.openfuture.chain.service.BaseStakeholderService
import org.springframework.transaction.annotation.Transactional

abstract class DefaultBaseStakeholderService<E : Stakeholder, D : StakeholderDto>(
        private val repository: StakeholderRepository<E>
) : BaseStakeholderService<E, D> {

    @Transactional(readOnly = true)
    override fun getAll(): List<E> = repository.findAll()

    @Transactional(readOnly = true)
    override fun getByPublicKey(publicKey: String): E = repository.findOneByPublicKey(publicKey)
            ?: throw NotFoundException("Stakeholder with publicKey: $publicKey not exist!")

    @Transactional
    override fun save(entity: E): E = repository.save(entity)

    abstract override fun add(dto: D): E

}