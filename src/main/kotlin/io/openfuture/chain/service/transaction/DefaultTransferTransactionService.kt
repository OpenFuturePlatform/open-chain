package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.impl.TransferTransactionEntityConverter
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.repository.TransferTransactionRepository
import io.openfuture.chain.service.TransferTransactionService
import io.openfuture.chain.service.WalletService
import org.springframework.stereotype.Service

@Service
class DefaultTransferTransactionService(
    repository: TransferTransactionRepository,
    walletService: WalletService,
    nodeClock: NodeClock,
    entityConverter: TransferTransactionEntityConverter
) : DefaultBaseTransactionService<TransferTransaction, TransferTransactionData>(repository,
    walletService, nodeClock, entityConverter), TransferTransactionService {

    override fun toBlock(tx: TransferTransaction, block: MainBlock): TransferTransaction {
        return super.baseToBlock(tx, block)
    }

    override fun validate(dto: BaseTransactionDto<TransferTransactionData>) {
        this.baseValidate(dto.data, dto.senderSignature, dto.senderPublicKey)
    }

    override fun validate(request: BaseTransactionRequest<TransferTransactionData>) {
        this.baseValidate(request.data!!, request.senderSignature!!, request.senderPublicKey!!)
    }

}