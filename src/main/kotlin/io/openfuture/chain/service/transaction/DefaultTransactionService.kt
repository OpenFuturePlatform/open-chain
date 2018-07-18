package io.openfuture.chain.service.transaction

import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.repository.BaseTransactionRepository
import io.openfuture.chain.service.BaseTransactionService
import io.openfuture.chain.service.WalletService
import org.springframework.stereotype.Service

@Service
class DefaultTransactionService(
    repository: BaseTransactionRepository<BaseTransaction>,
    walletService: WalletService
) : DefaultBaseTransactionService<BaseTransaction>(repository, walletService), BaseTransactionService<BaseTransaction> {

    override fun beforeAddToBlock(tx: BaseTransaction) {
        updateWalletBalance(tx.senderAddress, tx.recipientAddress, tx.amount)
    }

}