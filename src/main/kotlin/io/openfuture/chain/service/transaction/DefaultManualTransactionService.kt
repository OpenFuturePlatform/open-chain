package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.TransactionEntityConverter
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.repository.BaseTransactionRepository
import io.openfuture.chain.service.ManualTransactionService
import io.openfuture.chain.service.WalletService
import org.springframework.transaction.annotation.Transactional

abstract class DefaultManualTransactionService<Entity : BaseTransaction, Data : BaseTransactionData>(
    repository: BaseTransactionRepository<Entity>,
    walletService: WalletService,
    entityConverter: TransactionEntityConverter<Entity, Data>,
    private val nodeClock: NodeClock
) : DefaultBaseTransactionService<Entity, Data>(repository, walletService, entityConverter),
    ManualTransactionService<Entity, Data> {

    @Transactional
    override fun add(dto: BaseTransactionDto<Data>): Entity {
        //todo need to add validation
        val transaction = repository.findOneByHash(dto.hash)
        if (null != transaction) {
            return transaction
        }

        return saveAndBroadcast(entityConverter.toEntity(dto))
    }

    @Transactional
    override fun add(request: BaseTransactionRequest<Data>): Entity {
        return saveAndBroadcast(entityConverter.toEntity(nodeClock.networkTime(), request))
    }

    private fun saveAndBroadcast(tx: Entity): Entity {
        return repository.save(tx)
        //todo: networkService.broadcast(transaction.toMessage)
    }

}