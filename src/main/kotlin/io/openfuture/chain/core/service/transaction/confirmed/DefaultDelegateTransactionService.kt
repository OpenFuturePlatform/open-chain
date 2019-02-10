package io.openfuture.chain.core.service.transaction.confirmed

import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.repository.DelegateTransactionRepository
import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.core.sync.BlockchainLock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultDelegateTransactionService(
    private val repository: DelegateTransactionRepository
) : DefaultExternalTransactionService<DelegateTransaction, DelegateTransactionRepository>(repository), DelegateTransactionService {

    @Transactional
    override fun commit(tx: DelegateTransaction, receipt: Receipt): DelegateTransaction {
        BlockchainLock.writeLock.lock()
        try {
            val persistTx = repository.findOneByHash(tx.hash)
            if (null != persistTx) {
                return persistTx
            }

            val utx = uRepository.findOneByHash(tx.hash)
            if (null != utx) {
                return confirm(utx, tx)
            }

            return repository.save(tx)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

}