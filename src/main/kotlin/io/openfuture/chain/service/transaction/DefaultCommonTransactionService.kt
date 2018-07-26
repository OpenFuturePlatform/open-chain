package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.BaseTransactionEntityConverter
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.exception.LogicException
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.exception.ValidationException
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.repository.BaseTransactionRepository
import io.openfuture.chain.service.BaseTransactionService
import io.openfuture.chain.service.CommonTransactionService
import io.openfuture.chain.service.WalletService
import io.openfuture.chain.util.TransactionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

abstract class DefaultCommonTransactionService<Entity : BaseTransaction, Data : BaseTransactionData>(
    protected val repository: BaseTransactionRepository<Entity>,
    protected open val entityConverter: BaseTransactionEntityConverter<Entity, Data>
) : CommonTransactionService<Entity, Data> {

    @Autowired
    protected lateinit var nodeClock: NodeClock

    @Autowired
    protected lateinit var walletService: WalletService

    @Autowired
    private lateinit var consensusProperties: ConsensusProperties

    @Autowired
    private lateinit var baseService: BaseTransactionService


    @Transactional(readOnly = true)
    override fun get(hash: String): Entity = repository.findOneByHash(hash)
        ?: throw NotFoundException("Transaction with hash: $hash not exist!")

    @Transactional(readOnly = true)
    override fun isExists(hash: String) : Boolean = null != repository.findOneByHash(hash)

    @Transactional(readOnly = true)
    override fun getAllPending(): MutableSet<Entity> {
        return repository.findAllByBlockIsNull()
    }

    @Transactional
    override fun add(dto: BaseTransactionDto<Data>): Entity {
        val transaction = repository.findOneByHash(dto.hash)
        if (null != transaction) {
            return transaction
        }
        validate(dto)
        return saveAndBroadcast(entityConverter.toEntity(dto))
    }

    @Transactional
    override fun toBlock(dto: BaseTransactionDto<Data>, block: MainBlock) {
        if (!isExists(dto.hash)) {
            return
        }

        val tx = entityConverter.toEntity(dto)
        tx.block = block
        walletService.updateBalance(tx.senderAddress, tx.recipientAddress, tx.amount, tx.fee)
        repository.save(tx)
    }

    @Transactional
    override fun toBlock(tx: Entity, block: MainBlock) {
        val persistTx = this.get(tx.hash)

        if (null != persistTx.block) {
            throw LogicException("Transaction with hash: ${tx.hash} already belong to block!")
        }
        persistTx.block = block
        walletService.updateBalance(persistTx.senderAddress, persistTx.recipientAddress, persistTx.amount, persistTx.fee)
        repository.save(persistTx)
    }

    open fun validate(dto: BaseTransactionDto<Data>) {
        if (!isValidHash(dto.data, dto.senderSignature, dto.senderPublicKey, dto.hash)) {
            throw ValidationException("Invalid transaction hash")
        }
        commonValidate(dto.data, dto.senderSignature, dto.senderPublicKey)
    }

    protected fun saveAndBroadcast(tx: Entity): Entity {
        return repository.save(tx)
        //todo: networkService.broadcast(transaction.toMessage)
    }

    protected fun commonValidate(data: Data, signature: String, publicKey: String) {
        //todo need to add address validation

        if (!isValidSenderBalance(data.senderAddress, data.amount)) {
            throw ValidationException("Invalid wallet balance")
        }

        if (!isValidaSignature(data, signature, publicKey)) {
            throw ValidationException("Invalid transaction signature")
        }
    }

    private fun isValidHash(data: Data, publicKey: String, signature: String, hash: String): Boolean {
        return TransactionUtils.createHash(data, publicKey, signature) == hash
    }

    private fun isValidaSignature(data: Data, publicKey: String, signature: String): Boolean {
        return SignatureManager.verify(data.getBytes(), signature, HashUtils.fromHexString(publicKey))
    }

    private fun isValidSenderBalance(senderAddress: String, amount: Long): Boolean {
        if (consensusProperties.genesisAddress!! == senderAddress) {
            return true
        }

        val balance = walletService.getBalanceByAddress(senderAddress)
        val unconfirmedOutput = baseService.getAllPending()
            .filter { it.senderAddress == senderAddress }
            .map { it.amount + it.fee }
            .sum()

        val unspentBalance = balance - unconfirmedOutput
        if (unspentBalance < amount) {
            return false
        }
        return true
    }

}