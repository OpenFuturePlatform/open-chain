package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.model.entity.transaction.BaseTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.TransactionService
import io.openfuture.chain.core.service.WalletService
import io.openfuture.chain.core.util.TransactionUtils
import io.openfuture.chain.crypto.service.CryptoService
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.component.node.NodeClock
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.beans.factory.annotation.Autowired

abstract class BaseTransactionService<T : Transaction, U : UnconfirmedTransaction>(
    protected val repository: TransactionRepository<T>,
    protected val unconfirmedRepository: UTransactionRepository<U>
) {

    @Autowired
    protected lateinit var baseService: TransactionService

    @Autowired
    protected lateinit var clock: NodeClock

    @Autowired
    protected lateinit var walletService: WalletService

    @Autowired
    private lateinit var cryptoService: CryptoService


    open fun add(utx: U): U {
        if (!isValid(utx)) {
            throw ValidationException("Transaction is invalid!")
        }
        return unconfirmedRepository.save(utx)
    }

    open fun add(tx: T): T {
        if (!isValid(tx)) {
            throw ValidationException("Transaction is invalid!")
        }
        updateBalanceByFee(tx)
        return repository.save(tx)
    }

    abstract fun isValid(utx: U): Boolean

    abstract fun isValid(tx: T): Boolean

    protected fun toBlock(utx: U, tx: T): T {
        unconfirmedRepository.delete(utx)
        return add(tx)
    }

    protected fun isValidBase(tx: BaseTransaction): Boolean {
        return isValidAddress(tx.header.senderAddress, tx.senderPublicKey)
            && isValidFee(tx.header.senderAddress, tx.header.fee)
            && isValidHash(tx.header.timestamp, tx.header.fee, tx.header.senderAddress, tx.getPayload(), tx.hash)
            && isValidSignature(tx.hash, tx.senderSignature, tx.senderPublicKey)
    }

    protected fun isExists(hash: String): Boolean {
        val persistUtx = unconfirmedRepository.findOneByHash(hash)
        val persistTx = repository.findOneByHash(hash)
        return null != persistUtx || null != persistTx
    }

    private fun updateBalanceByFee(tx: BaseTransaction) {
        walletService.decreaseBalance(tx.header.senderAddress, tx.header.fee)
    }

    private fun isValidAddress(senderAddress: String, senderPublicKey: String): Boolean {
        return cryptoService.isValidAddress(senderAddress, ByteUtils.fromHexString(senderPublicKey))
    }

    private fun isValidFee(senderAddress: String, fee: Long): Boolean {
        val balance = walletService.getBalanceByAddress(senderAddress)
        val unspentBalance = balance - baseService.getAllUnconfirmedByAddress(senderAddress).map { it.header.fee }.sum()
        return fee in 0..unspentBalance
    }

    private fun isValidHash(timestamp: Long, fee: Long, senderAddress: String, payload: TransactionPayload, hash: String): Boolean {
        return TransactionUtils.generateHash(timestamp, fee, senderAddress, payload) == hash
    }

    private fun isValidSignature(hash: String, signature: String, publicKey: String): Boolean {
        return SignatureUtils.verify(ByteUtils.fromHexString(hash), signature, ByteUtils.fromHexString(publicKey))
    }

}