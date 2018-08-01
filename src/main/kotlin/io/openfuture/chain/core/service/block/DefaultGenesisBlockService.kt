package io.openfuture.chain.core.service.block

import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.repository.GenesisBlockRepository
import io.openfuture.chain.core.service.CommonBlockService
import io.openfuture.chain.core.service.DefaultDelegateService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.network.domain.application.block.GenesisBlockMessage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultGenesisBlockService(
    private val repository: GenesisBlockRepository,
    val commonBlockService: CommonBlockService,
    val delegateService: DefaultDelegateService
) : GenesisBlockService {


    @Transactional(readOnly = true)
    override fun getLast(): GenesisBlock = repository.findFirstByOrderByHeightDesc()
        ?: throw NotFoundException("Last block not found")

    @Transactional
    override fun save(block: GenesisBlock): GenesisBlock = repository.save(block)

    override fun add(dto: GenesisBlockMessage) {
        repository.save(dto.toEntity())
    }

    @Transactional(readOnly = true)
    override fun isValid(block: GenesisBlock): Boolean {
        val lastBlock = getLast()
        val blockFound = commonBlockService.get(block.hash) as? GenesisBlock

        return (blockFound != null
            && isValidEpochIndex(lastBlock, block)
            && isValidateActiveDelegates(block))
    }

    private fun isValidEpochIndex(lastBlock: GenesisBlock, block: GenesisBlock): Boolean =
        (lastBlock.epochIndex + 1 == block.epochIndex)

    private fun isValidateActiveDelegates(block: GenesisBlock): Boolean {
        val activeDelegates = block.activeDelegates
        return activeDelegates == delegateService.getActiveDelegates()
    }

}