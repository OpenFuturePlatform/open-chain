package io.openfuture.chain.service

import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.block.MainBlockDto
import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.entity.block.Block
import io.openfuture.chain.entity.transaction.Transaction
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.BlockRepository
import io.openfuture.chain.util.BlockUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultBlockService (
        private val repository: BlockRepository,
        private val transactionService: TransactionService<Transaction>
) : BlockService {

    @Transactional(readOnly = true)
    override fun get(id: Int): Block = repository.getOne(id)
        ?: throw NotFoundException("Not found id $id")

    @Transactional(readOnly = true)
    override fun getAll(): MutableList<Block> =  repository.findAll()

    @Transactional(readOnly = true)
    override fun getLast(): Block = repository.findFirstByOrderByHeightDesc()
        ?: throw NotFoundException("Last block not exist!")

    @Transactional
    override fun add(dto: MainBlockDto): Block {
        val persistBlock = repository.save(dto.toEntity())
        val transactions = dto.transactions.map { transactionService.addToBlock(it.hash, persistBlock) }
        persistBlock.transactions.addAll(transactions)
        return persistBlock
    }

    @Transactional
    override fun create(transactions: MutableSet<out TransactionDto>): MainBlockDto {
        val previousBlock = getLast()
        val merkleRootHash = BlockUtils.calculateMerkleRoot(transactions)
        val time = System.currentTimeMillis()
        val hash = BlockUtils.calculateHash(previousBlock.hash, merkleRootHash, time, (previousBlock.height + 1))
        return MainBlockDto(HashUtils.bytesToHexString(hash), previousBlock.height + 1,
                previousBlock.hash, merkleRootHash, time, "", transactions)
    }

}