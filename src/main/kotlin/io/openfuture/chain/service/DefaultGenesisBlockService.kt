package io.openfuture.chain.service

import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.GenesisBlockRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultGenesisBlockService(
    val genesisBlockRepository: GenesisBlockRepository,
    val delegateService: DefaultDelegateService
) : BlockService<GenesisBlock> {

    @Transactional(readOnly = true)
    override fun get(hash: String): GenesisBlock  = genesisBlockRepository.findByHash(hash)
        ?: throw NotFoundException("Block with hash:$hash not found ")

    @Transactional(readOnly = true)
    override fun getLast(): GenesisBlock = genesisBlockRepository.findFirstByOrderByHeightDesc()
        ?: throw NotFoundException("Last Genesis block not exist!")

    @Transactional
    override fun save(block: GenesisBlock): GenesisBlock = genesisBlockRepository.save(block)

    override fun isValid(block: GenesisBlock): Boolean {
        val lastBlock = getLast()
        val blockFound = genesisBlockRepository.findByHash(block.hash)

        return (blockFound != null
            && isValidEpochIndex(lastBlock, block)
            && isValidateActiveDelegates(block))
    }

    private fun isValidEpochIndex(lastBlock: GenesisBlock, block: GenesisBlock): Boolean
        = (lastBlock.epochIndex + 1 == block.epochIndex)

    private fun isValidateActiveDelegates(block: GenesisBlock): Boolean {
        val activeDelegates = block.activeDelegates
        return activeDelegates == delegateService.getActiveDelegates()
    }

}