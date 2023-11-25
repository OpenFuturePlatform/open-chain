package io.openfuture.chain.tendermint.service

import io.openfuture.chain.core.model.entity.tendermint.TendermintTransaction
import io.openfuture.chain.core.model.entity.tendermint.TendermintTransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.repository.TendermintTransactionRepository
import io.openfuture.chain.core.service.TendermintTransactionManager
import io.openfuture.chain.core.service.TendermintTransferTransactionService
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultTendermintTransactionManager(
    private val tendermintTransferTransactionService: TendermintTransferTransactionService,
    private val uRepository: TendermintTransactionRepository<TendermintTransaction>
) : TendermintTransactionManager {
    override fun getUnconfirmedBalanceBySenderAddress(address: String): Long {
        BlockchainLock.readLock.lock()
        try {
            return uRepository.findAllBySenderAddress(address).asSequence().map {
                it.fee + when (it) {
                    is UnconfirmedTransferTransaction -> it.getPayload().amount
                    is UnconfirmedDelegateTransaction -> it.getPayload().amount
                    else -> 0
                }
            }.sum()
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    override fun <uT : TendermintTransaction> check(uTx: uT): Boolean {
        val unconfirmedBalance = getUnconfirmedBalanceBySenderAddress(uTx.senderAddress)
        return when (uTx) {
            is TendermintTransferTransaction -> tendermintTransferTransactionService.check(uTx, unconfirmedBalance)
            else -> false
        }
    }

    override fun <uT : TendermintTransferTransaction> add(uTx: uT): Boolean {

        val unconfirmedBalance = getUnconfirmedBalanceBySenderAddress(uTx.senderAddress)
        return tendermintTransferTransactionService.add(uTx, unconfirmedBalance)
    }

    override fun getAllTransferTransactions(
        address: String
    ): List<TendermintTransferTransaction> {
        return tendermintTransferTransactionService.getAllBySenderAddress(address)
    }

}