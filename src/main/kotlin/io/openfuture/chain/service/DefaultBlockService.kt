package io.openfuture.chain.service

import io.openfuture.chain.entity.Block
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.BlockRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultBlockService(
    private val repository: BlockRepository,
    private val wallerService: WalletService
) : BlockService {

    @Transactional(readOnly = true)
    override fun get(hash: String): Block = repository.findByHash(hash)
        ?: throw NotFoundException("Block with hash:$hash not found")

    @Transactional(readOnly = true)
    override fun getLast(): Block = repository.findFirstByOrderByHeightDesc()
        ?: throw NotFoundException("Last block not found!")

    @Transactional(readOnly = true)
    override fun getLastGenesis(): Block {
        TODO("not implemented")
    }

    @Transactional
    override fun save(block: Block): Block {
        val savedBlock = repository.save(block)

        savedBlock.transactions.forEach {
            wallerService.updateByTransaction(it)
        }

        return savedBlock
    }

}