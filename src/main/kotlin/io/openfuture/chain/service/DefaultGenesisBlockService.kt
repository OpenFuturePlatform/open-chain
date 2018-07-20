package io.openfuture.chain.service

import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.BlockRepository
import io.openfuture.chain.repository.GenesisBlockRepository
import org.springframework.stereotype.Service

@Service
class DefaultGenesisBlockService(
    val genesisBlockRepository: GenesisBlockRepository
) : GenesisBlockService {

    override fun getLast(): GenesisBlock =
        genesisBlockRepository.findFirstByOrderByHeightDesc()
            ?: throw NotFoundException("Last Genesis block not exist!")

}