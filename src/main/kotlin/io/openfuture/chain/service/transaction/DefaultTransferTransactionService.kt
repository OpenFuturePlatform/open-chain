package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.impl.TransferTransactionEntityConverter
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import io.openfuture.chain.entity.MainBlock
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
    entityConverter: TransferTransactionEntityConverter,
    nodeClock: NodeClock
) : DefaultManualTransactionService<TransferTransaction, TransferTransactionData>(repository, walletService,
    entityConverter, nodeClock), TransferTransactionService {

    @Transactional
    override fun addToBlock(tx: TransferTransaction, block: MainBlock): TransferTransaction {
        return this.commonAddToBlock(tx, block)
    }

}