package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.TransactionEntityConverter
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.exception.LogicException
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.BaseTransactionRepository
import io.openfuture.chain.service.BaseTransactionService
import io.openfuture.chain.service.WalletService
import org.springframework.transaction.annotation.Transactional

abstract class DefaultBaseTransactionService<Entity : BaseTransaction, Data : BaseTransactionData>(
    protected val repository: BaseTransactionRepository<Entity>,
    protected val walletService: WalletService,
    private val nodeClock: NodeClock,
    private val entityConverter: TransactionEntityConverter<Entity, Data>
) : BaseTransactionService<Entity, Data> {

    @Transactional(readOnly = true)
    override fun getAllPending(): MutableSet<Entity> {
        return repository.findAllByBlockIsNull()
    }

    @Transactional(readOnly = true)
    override fun get(hash: String): Entity = repository.findOneByHash(hash)
        ?: throw NotFoundException("Transaction with hash: $hash not exist!")

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

    @Transactional
    override fun add(data: Data): Entity {
        return saveAndBroadcast(entityConverter.toEntity(nodeClock.networkTime(), data))
    }

    protected fun commonAddToBlock(tx: Entity, block: MainBlock): Entity {
        val persistTx = this.get(tx.hash)

        if (null != persistTx.block) {
            throw LogicException("Transaction with hash: ${tx.hash} already belong to block!")
        }
        persistTx.block = block
        walletService.updateBalance(persistTx.senderAddress, persistTx.recipientAddress, persistTx.amount)
        return repository.save(persistTx)
    }

    private fun saveAndBroadcast(tx: Entity): Entity {
        return repository.save(tx)
        //todo: networkService.broadcast(transaction.toMessage)
    }

}