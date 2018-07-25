package io.openfuture.chain.service.transaction.unconfirmed

import io.openfuture.chain.component.converter.transaction.ManualTransactionEntityConverter
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.unconfirmed.UTransaction
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.UTransactionRepository
import io.openfuture.chain.service.UTransactionService
import io.openfuture.chain.service.WalletService
import org.springframework.transaction.annotation.Transactional

abstract class DefaultUTransactionService<Entity : UTransaction, Data : BaseTransactionData>(
    protected val repository: UTransactionRepository<Entity>,
    protected val walletService: WalletService,
    private val nodeClock: NodeClock,
    private val entityConverter: ManualTransactionEntityConverter<Entity, Data>
) : UTransactionService<Entity, Data> {

    @Transactional(readOnly = true)
    override fun getAll(): MutableSet<Entity> = repository.findAll().toMutableSet()

    @Transactional(readOnly = true)
    override fun get(hash: String): Entity = repository.findOneByHash(hash)
        ?: throw NotFoundException("Unconfirmed transaction with hash: $hash not exist!")

    @Transactional
    override fun add(dto: BaseTransactionDto<Data>): Entity {
        //todo need to add validation
        var transaction = repository.findOneByHash(dto.hash)
        if (null != transaction) {
            return transaction
        }

        transaction = (entityConverter.toEntity(dto))
        process(transaction)
        return saveAndBroadcast(transaction)
    }

    @Transactional
    override fun add(request: BaseTransactionRequest<Data>): Entity {
        val transaction = entityConverter.toEntity(nodeClock.networkTime(), request)
        process(transaction)
        return saveAndBroadcast(transaction)
    }

    @Transactional
    override fun add(data: Data): Entity {
        val transaction = entityConverter.toEntity(nodeClock.networkTime(), data)
        process(transaction)
        return saveAndBroadcast(entityConverter.toEntity(nodeClock.networkTime(), data))
    }

    private fun saveAndBroadcast(tx: Entity): Entity {
        return repository.save(tx)
        //todo: networkService.broadcast(transaction.toMessage)
    }

}