package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.transaction.TransferTransactionDto
import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.repository.TransferTransactionRepository
import io.openfuture.chain.service.TransferTransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultTransferTransactionService(
    repository: TransferTransactionRepository,
    private val nodeClock: NodeClock
) : DefaultBaseTransactionService<TransferTransaction>(repository), TransferTransactionService {

    @Transactional
    override fun add(dto: TransferTransactionDto): TransferTransaction = repository.save(TransferTransaction.of(dto))

    override fun create(data: TransferTransactionData): TransferTransactionDto = TransferTransactionDto.of(nodeClock.networkTime(), data)

}