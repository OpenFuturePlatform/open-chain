package io.openfuture.chain.service.stakeholder

import io.openfuture.chain.domain.stakeholder.StakeholderDto
import io.openfuture.chain.entity.account.Stakeholder
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.StakeholderRepository
import io.openfuture.chain.service.BaseStakeholderService
import org.springframework.transaction.annotation.Transactional

abstract class DefaultBaseStakeholderService<Entity : Stakeholder, Dto : StakeholderDto>(
    private val repository: StakeholderRepository<Entity>
) : BaseStakeholderService<Entity, Dto> {

    @Transactional(readOnly = true)
    override fun getAll(): List<Entity> = repository.findAll()

    @Transactional(readOnly = true)
    override fun getByPublicKey(publicKey: String): Entity = repository.findOneByPublicKey(publicKey)
        ?: throw NotFoundException("Stakeholder with publicKey: $publicKey not exist!")

    @Transactional
    override fun save(entity: Entity): Entity = repository.save(entity)

}