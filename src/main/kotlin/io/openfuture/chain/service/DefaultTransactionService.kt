package io.openfuture.chain.service

import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.domain.transaction.TransactionData
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.exception.LogicException
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.exception.ValidationException
import io.openfuture.chain.repository.TransactionRepository
import io.openfuture.chain.util.HashUtils
import io.openfuture.chain.util.TransactionUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultTransactionService(
    private val repository: TransactionRepository
): TransactionService {

    @Transactional(readOnly = true)
    override fun getAllPending(): List<TransactionDto> {
        return repository.findAllByBlockIsNull().map { TransactionDto(it) }
    }

    @Transactional(readOnly = true)
    override fun get(hash: String): Transaction = repository.findOneByHash(hash)
            ?: throw NotFoundException("Transaction with hash: $hash not exist!")

    @Transactional
    override fun add(dto: TransactionDto): Transaction {
        if (!this.isValid(dto)) {
            throw ValidationException("Transaction is not valid!")
        }

        return repository.save(Transaction.of(dto))
    }

    override fun create(data: TransactionData): TransactionDto {
        val hash = TransactionUtils.generateHash(data)
        return TransactionDto(data, hash)
    }

    @Transactional
    override fun addToBlock(hash: String, block: Block): Transaction {
        val persisBlock = this.get(hash)
        if (null != persisBlock.block) {
            throw LogicException("Transaction with hash: $hash already belong to block!")
        }

        persisBlock.block = block
        return repository.save(persisBlock)
    }

    override fun isValid(dto: TransactionDto): Boolean {
        if (!TransactionUtils.isValidHash(dto.hash, dto.data)) {
            return false
        }
        return true
    }

    override fun isExists(hash: String): Boolean {
        return null != repository.findOneByHash(hash)
    }

}