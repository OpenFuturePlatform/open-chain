package io.openfuture.chain.core.sync

import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.repository.GenesisBlockRepository
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class CurrentGenesisBlock(
    private val genesisBlockRepository: GenesisBlockRepository
) {

    @Volatile lateinit var block: GenesisBlock


    @PostConstruct
    private fun init() {
        block = genesisBlockRepository.findFirstByOrderByHeightDesc()!!
    }

}