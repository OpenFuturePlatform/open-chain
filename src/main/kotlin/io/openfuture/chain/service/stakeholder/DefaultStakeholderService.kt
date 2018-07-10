package io.openfuture.chain.service.stakeholder

import io.openfuture.chain.domain.stakeholder.StakeholderDto
import io.openfuture.chain.entity.account.Stakeholder
import io.openfuture.chain.repository.StakeholderRepository
import io.openfuture.chain.service.StakeholderService
import org.springframework.stereotype.Service

@Service
class DefaultStakeholderService (
        private val repository: StakeholderRepository<Stakeholder>
) : DefaultBaseStakeholderService<Stakeholder, StakeholderDto>(repository), StakeholderService {

    override fun add(dto: StakeholderDto): Stakeholder  = repository.save(Stakeholder.of(dto))

}