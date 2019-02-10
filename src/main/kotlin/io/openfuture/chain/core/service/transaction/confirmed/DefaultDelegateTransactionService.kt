package io.openfuture.chain.core.service.transaction.confirmed

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.ReceiptResult
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.repository.DelegateTransactionRepository
import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.core.service.UDelegateTransactionService
import io.openfuture.chain.core.sync.BlockchainLock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultDelegateTransactionService(
    private val repository: DelegateTransactionRepository,
    private val uDelegateTransactionService: UDelegateTransactionService,
    private val consensusProperties: ConsensusProperties
) : DefaultExternalTransactionService<DelegateTransaction, UnconfirmedDelegateTransaction, DelegateTransactionRepository,
    UDelegateTransactionService>(repository, uDelegateTransactionService), DelegateTransactionService {

    @Transactional
    override fun commit(tx: DelegateTransaction, receipt: Receipt): DelegateTransaction {
        BlockchainLock.writeLock.lock()
        try {
            val persistTx = repository.findOneByHash(tx.hash)
            if (null != persistTx) {
                return persistTx
            }

            val utx = uDelegateTransactionService.findByHash(tx.hash)
            if (null != utx) {
                return confirm(utx, tx)
            }

            return repository.save(tx)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    override fun process(uTx: UnconfirmedDelegateTransaction, delegateWallet: String): Receipt {
        stateManager.updateWalletBalanceByAddress(uTx.senderAddress, -(uTx.getPayload().amount + uTx.fee))
        stateManager.updateWalletBalanceByAddress(consensusProperties.genesisAddress!!, uTx.getPayload().amount)
        stateManager.addDelegate(uTx.getPayload().delegateKey, uTx.senderAddress, uTx.timestamp)

        return generateReceipt(uTx, delegateWallet)
    }

    private fun generateReceipt(uTx: UnconfirmedDelegateTransaction, delegateWallet: String): Receipt {
        val results = listOf(
            ReceiptResult(uTx.senderAddress, consensusProperties.genesisAddress!!, uTx.getPayload().amount,
                uTx.getPayload().delegateKey),
            ReceiptResult(uTx.senderAddress, delegateWallet, uTx.fee)
        )
        return getReceipt(uTx.hash, results)
    }

}