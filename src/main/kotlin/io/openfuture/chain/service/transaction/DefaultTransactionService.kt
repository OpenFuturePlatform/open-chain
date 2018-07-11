package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.domain.transaction.data.TransactionData
import io.openfuture.chain.entity.transaction.Transaction
import io.openfuture.chain.repository.TransactionRepository
import io.openfuture.chain.service.TransactionService
import io.openfuture.chain.util.TransactionUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultTransactionService(
    repository: TransactionRepository<Transaction>,
    private val nodeClock: NodeClock
) : DefaultBaseTransactionService<Transaction, TransactionDto, TransactionData>(repository), TransactionService {

    @Transactional
    override fun add(dto: TransactionDto): Transaction {
        return repository.save(Transaction.of(dto))
    }

    override fun create(data: TransactionData): TransactionDto {
        val networkTime = nodeClock.networkTime()
        val hash = TransactionUtils.calculateHash(networkTime, data)
        return TransactionDto(networkTime, data.amount, data.recipientKey, data.senderKey, data.senderSignature, hash)
    }

}