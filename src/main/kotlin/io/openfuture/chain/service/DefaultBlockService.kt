package io.openfuture.chain.service

import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.block.MainBlockDto
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.entity.block.Block
import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.BlockRepository
import io.openfuture.chain.util.BlockUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultBlockService(
    private val blockRepository: BlockRepository
) : BlockService {

    @Transactional(readOnly = true)
    override fun get(hash: String): Block = blockRepository.findByHash(hash)
        ?: throw NotFoundException("Block with hash:$hash not found")

    @Transactional(readOnly = true)
    override fun getLast(): Block = blockRepository.findFirstByOrderByHeightDesc()
        ?: throw NotFoundException("Last block not found!")

    @Transactional(readOnly = true)
    override fun getLastGenesis(): Block {
        TODO("not implemented")
    }

}