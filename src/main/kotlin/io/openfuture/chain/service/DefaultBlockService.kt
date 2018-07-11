package io.openfuture.chain.service

import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.BlockType
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.BlockRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultBlockService(
    private val blockRepository: BlockRepository<Block>,
    private val transactionService: TransactionService
) : BlockService {

    @Transactional(readOnly = true)
    override fun get(id: Int): Block =
        blockRepository.getOne(id)
            ?: throw NotFoundException("Not found id $id")

    @Transactional(readOnly = true)
    override fun getAll(): MutableList<Block> = blockRepository.findAll()

    @Transactional(readOnly = true)
    override fun getLast(): Block =
        blockRepository.findFirstByOrderByHeightDesc()
            ?: throw NotFoundException("Last block not exist!")

    @Transactional(readOnly = true)
    override fun getLastGenesisBlock(): GenesisBlock =
        blockRepository.findFirstByTypeIdOrderByHeight(BlockType.GENESIS.typeId) as? GenesisBlock
            ?: throw NotFoundException("Last Genesis block not exist!")

    @Transactional
    override fun save(block: Block): Block {
        val savedBlock = blockRepository.save(block)
        if (block is MainBlock) {
            val transactions = block.transactions
            for (transaction in transactions) {
                transaction.blockId = savedBlock.id
            }
            transactionService.saveAll(transactions)
        }
        return savedBlock
    }

}