package io.openfuture.chain.service.transaction

import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.Transaction
import io.openfuture.chain.entity.transaction.unconfirmed.UTransaction
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.TransactionRepository
import io.openfuture.chain.repository.UTransactionRepository
import io.openfuture.chain.service.BaseTransactionService
import io.openfuture.chain.service.TransactionService
import io.openfuture.chain.service.WalletService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

abstract class DefaultTransactionService<Entity : Transaction, UEntity : UTransaction, Data: BaseTransactionData,
    Dto: BaseTransactionDto<Entity, UEntity, Data>>(
    private val repository: TransactionRepository<Entity>,
    private val uRepository: UTransactionRepository<UEntity>
) : TransactionService<Entity, UEntity, Data, Dto> {

    @Autowired
    protected lateinit var walletService: WalletService

    @Autowired
    private lateinit var baseService: BaseTransactionService


    @Transactional
    override fun toBlock(dto: Dto, block: MainBlock) {
        if (baseService.isExists(dto.hash)) {
            return
        }

        val tx = dto.toEntity()
        tx.block = block
        walletService.updateBalance(tx.senderAddress, tx.recipientAddress, tx.amount, tx.fee)
        repository.save(tx)
    }

    @Transactional
    override fun toBlock(tx: Entity, block: MainBlock) {
        tx.block = block
        deleteUnconfirmedAndSave(tx)
    }

    private fun deleteUnconfirmedAndSave(tx: Entity) {
        val uTx = getUnconfirmedTransaction(tx.hash)
        updateBalance(uTx)
        uRepository.delete(uTx)
        repository.save(tx)
    }

    private fun updateBalance(tx: UEntity) {
        walletService.updateBalance(tx.senderAddress, tx.recipientAddress, tx.amount, tx.fee)
    }

    private fun getUnconfirmedTransaction(hash: String): UEntity = uRepository.findOneByHash(hash)
        ?: throw NotFoundException("Unconfirmed transaction with hash: $hash not exist!")

}