package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.model.dto.transaction.BaseTransactionDto
import io.openfuture.chain.core.model.dto.transaction.DelegateTransactionDto
import io.openfuture.chain.core.model.dto.transaction.TransferTransactionDto
import io.openfuture.chain.core.model.dto.transaction.VoteTransactionDto
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.core.service.TransactionService
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.core.service.VoteTransactionService
import io.openfuture.chain.rpc.domain.transaction.BaseTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.DelegateTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.TransferTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.VoteTransactionRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultTransactionService(
    private val repository: TransactionRepository<Transaction>,
    private val unconfirmedRepository: UTransactionRepository<UTransaction>
) : TransactionService {

//    @Transactional
//    override fun add(dto: BaseTransactionDto): UTransaction {
//        return when (dto) {
//            is VoteTransactionDto -> voteTransactionService.add(dto)
//            is TransferTransactionDto -> transferTransactionService.add(dto)
//            is DelegateTransactionDto -> delegateTransactionService.add(dto)
//            else -> throw IllegalStateException("Unknown transaction type")
//        }
//    }
//
//    @Transactional
//    override fun add(request: BaseTransactionRequest): UTransaction {
//        return when (request) {
//            is VoteTransactionRequest -> voteTransactionService.add(request)
//            is TransferTransactionRequest -> transferTransactionService.add(request)
//            is DelegateTransactionRequest -> delegateTransactionService.add(request)
//            else -> throw IllegalStateException("Unknown transaction type")
//        }
//    }
//
//    @Transactional
//    override fun toBlock(dto: BaseTransactionDto, block: MainBlock) {
//        when (dto) {
//            is VoteTransactionDto -> voteTransactionService.toBlock(dto.hash, block)
//            is TransferTransactionDto -> transferTransactionService.toBlock(dto.hash, block)
//            is DelegateTransactionDto -> delegateTransactionService.toBlock(dto.hash, block)
//            else -> throw IllegalStateException("Unknown transaction type")
//        }
//    }

    @Transactional(readOnly = true)
    override fun getAllUnconfirmed(): MutableSet<UTransaction> {
        return unconfirmedRepository.findAllByOrderByFeeDesc()
    }

    @Transactional(readOnly = true)
    override fun getCount(): Long {
        return repository.count()
    }

}