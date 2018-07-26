package io.openfuture.chain.service.transaction.unconfirmed

import io.openfuture.chain.component.converter.transaction.TransactionEntityConverter
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.unconfirmed.UTransaction
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.exception.ValidationException
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.repository.UTransactionRepository
import io.openfuture.chain.service.BaseTransactionService
import io.openfuture.chain.service.CommonUTransactionService
import io.openfuture.chain.service.WalletService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

abstract class DefaultCommonUTransactionService<Entity : UTransaction, Data : BaseTransactionData, Converter : TransactionEntityConverter<Entity, Data>>(
    protected val repository: UTransactionRepository<Entity>,
    protected val entityConverter: Converter
) : CommonUTransactionService<Entity, Data> {

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
        ?: throw NotFoundException("Unconfirmed transaction with hash: $hash not exist!")

    @Transactional(readOnly = true)
    override fun getAll(): MutableSet<Entity> = repository.findAll().toMutableSet()

    @Transactional
    override fun add(dto: BaseTransactionDto<Data>): Entity {
        val transaction = repository.findOneByHash(dto.hash)
        if (null != transaction) {
            return transaction
        }
        validate(dto)
        return saveAndBroadcast(entityConverter.toEntity(dto))
    }

    protected fun saveAndBroadcast(tx: Entity): Entity {
        return repository.save(tx)
        //todo: networkService.broadcast(transaction.toMessage)
    }

    protected abstract fun validate(dto: BaseTransactionDto<Data>)

    protected fun baseValidate(dto: BaseTransactionDto<Data>) {
        if (!isValidHash(dto.data, dto.hash)) {
            throw ValidationException("Invalid transaction hash")
        }
        commonValidate(dto.data, dto.senderSignature, dto.senderPublicKey)
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

    private fun isValidHash(data: Data, hash: String): Boolean {
        return data.getHash() == hash
    }

    private fun isValidaSignature(data: Data, signature: String, publicKey: String): Boolean {
        return SignatureManager.verify(data.getBytes(), signature, HashUtils.fromHexString(publicKey))
    }

    private fun isValidSenderBalance(senderAddress: String, amount: Long): Boolean {
        if (consensusProperties.genesisAddress!! == senderAddress) {
            return true
        }

        val balance = walletService.getBalanceByAddress(senderAddress)
        val unconfirmedOutput = baseService.getPending()
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