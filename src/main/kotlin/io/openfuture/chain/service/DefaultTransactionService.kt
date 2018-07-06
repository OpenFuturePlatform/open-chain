package io.openfuture.chain.service

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.domain.transaction.TransactionData
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.exception.LogicException
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultTransactionService(
        private val repository: TransactionRepository,
        private val nodeClock: NodeClock
) : TransactionService {

    @Transactional(readOnly = true)
    override fun getAllPending(): List<Transaction> {
        return repository.findAllByBlockIsNull()
    }

    @Transactional(readOnly = true)
    override fun get(hash: String): Transaction = repository.findOneByHash(hash)
            ?: throw NotFoundException("Transaction with hash: $hash not exist!")

    @Transactional
    override fun addToBlock(hash: String, block: Block): Transaction {
        val persistTransaction = this.get(hash)
        if (null != persistTransaction.block) {
            throw LogicException("Transaction with hash: $hash already belong to block!")
        }

        persistTransaction.block = block
        return repository.save(persistTransaction)
    }
    @Transactional
    override fun add(dto: TransactionDto): Transaction {
        //todo need to add validation
        return repository.save(Transaction.of(dto))
    }

    override fun create(data: TransactionData): TransactionDto {
        val networkTime = nodeClock.networkTime()
        return TransactionDto.of(networkTime, data)
    }

}