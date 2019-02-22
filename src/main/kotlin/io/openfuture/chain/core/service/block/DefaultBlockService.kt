package io.openfuture.chain.core.service.block

import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.repository.BlockRepository
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
abstract class DefaultBlockService<T : Block>(
    private val repository: BlockRepository<T>
) : BlockService<T> {

    @Autowired protected lateinit var blockRepository: BlockRepository<Block>


    override fun getByHash(hash: String): T = repository.findOneByHash(hash)
        ?: throw NotFoundException("Block $hash not found")

    override fun getAll(request: PageRequest): Page<T> = repository.findAll(request)

    override fun getPreviousBlock(hash: String): T =
        repository.findFirstByHeightLessThanOrderByHeightDesc(getByHash(hash).height)
            ?: throw NotFoundException("Block before $hash not found")

    override fun getNextBlock(hash: String): T = repository.findFirstByHeightGreaterThan(getByHash(hash).height)
        ?: throw NotFoundException("Block after $hash not found")

    override fun getLast(): T = repository.findFirstByHeightLessThanOrderByHeightDesc(Long.MAX_VALUE)
        ?: throw NotFoundException("Last block not found")

    protected fun getLastBlock(): Block = blockRepository.findFirstByOrderByHeightDesc()

}