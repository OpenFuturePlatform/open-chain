package io.openfuture.chain.service

import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.GenesisBlockRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultGenesisBlockService(
    val genesisBlockRepository: GenesisBlockRepository
) : BlockService<GenesisBlock> {

    override fun get(hash: String): GenesisBlock  = genesisBlockRepository.findByHash(hash)
        ?: throw NotFoundException("Block with hash:$hash not found ")

    @Transactional(readOnly = true)
    override fun getLast(): GenesisBlock =
        genesisBlockRepository.findFirstByOrderByHeightDesc()
            ?: throw NotFoundException("Last Genesis block not exist!")

    override fun save(block: GenesisBlock): GenesisBlock = genesisBlockRepository.save(block)

    override fun isValid(block: GenesisBlock): Boolean = true

}