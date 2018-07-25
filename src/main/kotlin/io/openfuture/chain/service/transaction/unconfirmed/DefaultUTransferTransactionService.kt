package io.openfuture.chain.service.transaction.unconfirmed

import io.openfuture.chain.component.converter.transaction.unconfirmed.impl.UTransferTransactionEntityConverter
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import io.openfuture.chain.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.repository.UTransferTransactionRepository
import io.openfuture.chain.service.UTransferTransactionService
import io.openfuture.chain.service.WalletService
import org.springframework.stereotype.Service

@Service
class DefaultUTransferTransactionService(
    repository: UTransferTransactionRepository,
    walletService: WalletService,
    nodeClock: NodeClock,
    entityConverter: UTransferTransactionEntityConverter
) : DefaultUTransactionService<UTransferTransaction, TransferTransactionData>(repository,
    walletService, nodeClock, entityConverter), UTransferTransactionService {

    override fun process(tx: UTransferTransaction) = Unit

}