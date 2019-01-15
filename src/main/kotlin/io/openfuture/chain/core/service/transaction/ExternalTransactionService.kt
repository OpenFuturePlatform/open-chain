package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.exception.CoreException
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.INCORRECT_ADDRESS
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.StateService
import io.openfuture.chain.core.service.TransactionService
import io.openfuture.chain.crypto.service.CryptoService
import io.openfuture.chain.network.service.NetworkApiService
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.beans.factory.annotation.Autowired

abstract class ExternalTransactionService<T : Transaction, U : UnconfirmedTransaction>(
    protected val repository: TransactionRepository<T>,
    protected val unconfirmedRepository: UTransactionRepository<U>
) : BaseTransactionService() {

    @Autowired protected lateinit var stateService: StateService
    @Autowired protected lateinit var baseService: TransactionService
    @Autowired protected lateinit var transactionService: TransactionService
    @Autowired private lateinit var cryptoService: CryptoService
    @Autowired private lateinit var networkService: NetworkApiService


    protected fun add(utx: U): U {
        val persistTx = repository.findOneByFooterHash(utx.footer.hash)
        if (null != persistTx) {
            throw CoreException("Transaction already handled")
        }

        val persistUtx = unconfirmedRepository.findOneByFooterHash(utx.footer.hash)
        if (null != persistUtx) {
            return persistUtx
        }

        validate(utx)
        validateNew(utx)

        val savedUtx = save(utx)
        networkService.broadcast(savedUtx.toMessage())
        return savedUtx
    }

    abstract fun validateNew(utx: U)

    open fun validate(utx: U) {

        validateBase(utx.header, utx.externalPayload, utx.footer)

        if (!isValidAddress(utx.header.senderAddress, utx.footer.senderPublicKey)) {
            throw ValidationException("Incorrect sender address", INCORRECT_ADDRESS)
        }
    }

    open fun save(utx: U): U = unconfirmedRepository.save(utx)

    open fun save(tx: T): T = repository.save(tx)

    protected fun confirm(utx: U, tx: T): T {
        unconfirmedRepository.delete(utx)
        return save(tx)
    }

    protected fun isValidActualBalance(address: String, amount: Long): Boolean =
        stateService.getActualBalanceByAddress(address) >= amount

    private fun isValidAddress(senderAddress: String, senderPublicKey: String): Boolean =
        cryptoService.isValidAddress(senderAddress, ByteUtils.fromHexString(senderPublicKey))

}
