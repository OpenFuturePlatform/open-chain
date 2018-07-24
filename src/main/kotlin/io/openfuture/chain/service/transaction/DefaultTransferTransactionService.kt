package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.impl.TransferTransactionEntityConverter
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.repository.TransferTransactionRepository
import io.openfuture.chain.service.TransferTransactionService
import org.springframework.stereotype.Service

@Service
class DefaultTransferTransactionService(
    repository: TransferTransactionRepository,
    entityConverter: TransferTransactionEntityConverter
) : DefaultManualTransactionService<TransferTransaction, TransferTransactionData>(repository, entityConverter),
    TransferTransactionService {

    override fun toBlock(tx: TransferTransaction, block: MainBlock): TransferTransaction {
        return super.baseToBlock(tx, block)
    }

    override fun validate(dto: BaseTransactionDto<TransferTransactionData>) {
        this.baseValidate(dto)
    }

    override fun validate(request: BaseTransactionRequest<TransferTransactionData>) {
        this.baseValidate(request)
    }

}