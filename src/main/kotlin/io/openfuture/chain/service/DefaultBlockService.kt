package io.openfuture.chain.service

import io.openfuture.chain.entity.block.Block
import io.openfuture.chain.entity.block.GenesisBlock
import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.BlockRepository
import io.openfuture.chain.repository.GenesisBlockRepository
import io.openfuture.chain.repository.MainBlockRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultBlockService(
    private val blockRepository: BlockRepository<Block>,
    private val genesisBlockRepository: GenesisBlockRepository,
    private val mainBlockRepository: MainBlockRepository
) : BlockService {

    @Transactional(readOnly = true)
    override fun get(hash: String): Block = blockRepository.findByHash(hash)
        ?: throw NotFoundException("Block with hash:$hash not found")

    @Transactional(readOnly = true)
    override fun getLast(): MainBlock = mainBlockRepository.findFirstByOrderByHeightDesc()
        ?: throw NotFoundException("Last block not found!")

    @Transactional(readOnly = true)
    override fun getLastGenesis(): GenesisBlock = genesisBlockRepository.findFirstByOrderByHeightDesc()
        ?: throw NotFoundException("Last block not found!")

}