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

    @Autowired protected lateinit var clock: NodeClock

    @Autowired private lateinit var cryptoService: CryptoService
    @Autowired protected lateinit var walletService: WalletService
    @Autowired protected lateinit var baseService: TransactionService
    @Autowired protected lateinit var transactionService: TransactionService

    companion object {
        const val TRANSACTION_EXCEPTION_MESSAGE: String = "Transaction is invalid : "
    }


    protected fun save(utx: U): U {
        validate(utx)

        return unconfirmedRepository.save(utx)
    }

    protected fun save(tx: T): T {
        validate(tx)

        updateBalanceByFee(tx)
        return repository.save(tx)
    }

    protected fun confirmProcess(utx: U, tx: T): T {
        unconfirmedRepository.delete(utx)
        updateBalanceByFee(tx)
        return repository.save(tx)
    }

    protected fun isExists(hash: String): Boolean {
        val persistUtx = unconfirmedRepository.findOneByHash(hash)
        val persistTx = repository.findOneByHash(hash)
        return null != persistUtx || null != persistTx
    }

    open fun validate(utx: U) {
        this.validateBase(utx)
    }

    open fun validate(tx: T) {
        this.validateBase(tx)
    }

    private fun updateBalanceByFee(tx: BaseTransaction) {
        walletService.decreaseBalance(tx.senderAddress, tx.fee)
    }

    private fun validateBase(tx: BaseTransaction) {
        validateAddress(tx.senderAddress, tx.senderPublicKey)
        validateFee(tx.senderAddress, tx.fee)
        validateHash(tx.timestamp, tx.fee, tx.senderAddress, tx.getPayload(), tx.hash)
        validateSignature(tx.hash, tx.senderSignature, tx.senderPublicKey)
    }

    private fun validateAddress(senderAddress: String, senderPublicKey: String) {
        if (!cryptoService.isValidAddress(senderAddress, ByteUtils.fromHexString(senderPublicKey))) {
            throw ValidationException(TRANSACTION_EXCEPTION_MESSAGE + "incorrect sender address by current public key")
        }
    }

    private fun validateFee(senderAddress: String, fee: Long) {
        val balance = walletService.getBalanceByAddress(senderAddress)
        val unspentBalance = balance - baseService.getAllUnconfirmedByAddress(senderAddress).map { it.fee }.sum()

        if (unspentBalance < fee) {
            throw ValidationException(TRANSACTION_EXCEPTION_MESSAGE + "actual balance is less then fee")
        }
    }

    private fun validateHash(timestamp: Long, fee: Long, senderAddress: String, payload: TransactionPayload, hash: String) {
        if (TransactionUtils.generateHash(timestamp, fee, senderAddress, payload) != hash) {
            throw ValidationException(TRANSACTION_EXCEPTION_MESSAGE + "incorrect hash by all transaction fields")
        }
    }

    private fun validateSignature(hash: String, signature: String, publicKey: String) {
        if (!SignatureUtils.verify(ByteUtils.fromHexString(hash), signature, ByteUtils.fromHexString(publicKey))) {
            throw ValidationException(TRANSACTION_EXCEPTION_MESSAGE + "incorrect signature by hash and public key")
        }
    }

}
