package io.openfuture.chain.service.block

import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.repository.GenesisBlockRepository
import io.openfuture.chain.service.CommonBlockService
import io.openfuture.chain.service.DefaultDelegateService
import io.openfuture.chain.service.GenesisBlockService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultGenesisBlockService(
    repository: GenesisBlockRepository,
    val commonBlockService: CommonBlockService,
    val delegateService: DefaultDelegateService
) : DefaultBlockService<GenesisBlock>(repository), GenesisBlockService {

    @Transactional(readOnly = true)
    override fun isValid(block: GenesisBlock): Boolean {
        val lastBlock = getLast()
        val blockFound = commonBlockService.get(block.hash) as? GenesisBlock

        return (blockFound != null
            && isValidEpochIndex(lastBlock, block)
            && isValidateActiveDelegates(block))
    }

    private fun isValidEpochIndex(lastBlock: GenesisBlock, block: GenesisBlock): Boolean = (lastBlock.epochIndex + 1 == block.epochIndex)

    private fun isValidateActiveDelegates(block: GenesisBlock): Boolean {
        val activeDelegates = block.activeDelegates
        return activeDelegates == delegateService.getActiveDelegates()
    }

}