package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.transaction.TransferTransactionDto
import io.openfuture.chain.domain.rpc.transaction.TransferTransactionRequest
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.repository.TransferTransactionRepository
import io.openfuture.chain.service.TransferTransactionService
import io.openfuture.chain.service.WalletService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultTransferTransactionService(
    repository: TransferTransactionRepository,
    walletService: WalletService,
    private val nodeClock: NodeClock
) : DefaultBaseTransactionService<TransferTransaction>(repository, walletService), TransferTransactionService {

    @Transactional
    override fun add(dto: TransferTransactionDto) {
        save(TransferTransaction.of(dto))
    }

    override fun add(request: TransferTransactionRequest) {
        save(TransferTransaction.of(nodeClock.networkTime(), request))
    }

    private fun save(transaction: TransferTransaction) {
        repository.save(transaction)
    }

    override fun beforeAddToBlock(tx: TransferTransaction) {
        updateWalletBalance(tx.senderAddress, tx.recipientAddress, tx.amount)
    }

}