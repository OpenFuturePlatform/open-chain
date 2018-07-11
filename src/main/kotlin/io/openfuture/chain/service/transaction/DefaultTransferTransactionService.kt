package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.transaction.TransferTransactionDto
import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.repository.TransferTransactionRepository
import io.openfuture.chain.service.TransferTransactionService
import io.openfuture.chain.util.TransactionUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultTransferTransactionService(
    repository: TransferTransactionRepository,
    private val nodeClock: NodeClock
) : DefaultBaseTransactionService<TransferTransaction>(repository), TransferTransactionService {

    @Transactional
    override fun add(dto: TransferTransactionDto): TransferTransaction {
        return repository.save(TransferTransaction.of(dto))
    }

    override fun create(data: TransferTransactionData): TransferTransactionDto {
        val networkTime = nodeClock.networkTime()
        val hash = TransactionUtils.calculateHash(networkTime, data)
        return TransferTransactionDto(networkTime, data.amount, data.recipientKey, data.senderKey, data.senderSignature, hash)
    }

}