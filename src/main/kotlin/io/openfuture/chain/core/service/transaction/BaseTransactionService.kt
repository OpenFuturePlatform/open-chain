package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.*
import io.openfuture.chain.core.model.entity.transaction.BaseTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.TransactionService
import io.openfuture.chain.core.service.WalletService
import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import io.openfuture.chain.crypto.service.CryptoService
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.component.node.NodeClock
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.beans.factory.annotation.Autowired
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8

abstract class BaseTransactionService<T : Transaction, U : UnconfirmedTransaction>(
    protected val repository: TransactionRepository<T>,
    protected val unconfirmedRepository: UTransactionRepository<U>
) {

    @Autowired protected lateinit var clock: NodeClock

    @Autowired private lateinit var cryptoService: CryptoService
    @Autowired protected lateinit var walletService: WalletService
    @Autowired protected lateinit var baseService: TransactionService
    @Autowired protected lateinit var transactionService: TransactionService


    protected fun save(utx: U): U {
        check(utx)

        return unconfirmedRepository.save(utx)
    }

    protected fun save(tx: T): T {
        check(tx)

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

    open fun check(utx: U) {
        this.baseCheck(utx)
    }

    open fun check(tx: T) {
        this.baseCheck(tx)
    }

    private fun updateBalanceByFee(tx: BaseTransaction) {
        walletService.decreaseBalance(tx.senderAddress, tx.fee)
    }

    private fun baseCheck(tx: BaseTransaction) {
        checkAddress(tx.senderAddress, tx.senderPublicKey)
        checkFee(tx.senderAddress, tx.fee)
        checkHash(tx)
        checkSignature(tx.hash, tx.senderSignature, tx.senderPublicKey)
    }

    private fun checkAddress(senderAddress: String, senderPublicKey: String) {
        if (!cryptoService.isValidAddress(senderAddress, ByteUtils.fromHexString(senderPublicKey))) {
            throw ValidationException("Incorrect sender address", INCORRECT_ADDRESS)
        }
    }

    private fun checkFee(senderAddress: String, fee: Long) {
        val balance = walletService.getBalanceByAddress(senderAddress)
        val unspentBalance = balance - baseService.getAllUnconfirmedByAddress(senderAddress).map { it.fee }.sum()

        if (unspentBalance < fee) {
            throw ValidationException("Insufficient balance", INSUFFICIENT_BALANCE)
        }
    }

    private fun checkHash(tx: BaseTransaction) {
        if (createHash(tx.timestamp, tx.fee, tx.senderAddress, tx.getPayload()) != tx.hash) {
            throw ValidationException("Incorrect hash", INCORRECT_HASH)
        }
    }

    private fun checkSignature(hash: String, signature: String, publicKey: String) {
        if (!SignatureUtils.verify(ByteUtils.fromHexString(hash), signature, ByteUtils.fromHexString(publicKey))) {
            throw ValidationException("Incorrect signature", INCORRECT_SIGNATURE)
        }
    }

    private fun createHash(timestamp: Long, fee: Long, senderAddress: String, payload: TransactionPayload): String {
        val bytes = ByteBuffer.allocate(LONG_BYTES + LONG_BYTES + senderAddress.toByteArray(UTF_8).size + payload.getBytes().size)
            .putLong(timestamp)
            .putLong(fee)
            .put(senderAddress.toByteArray(UTF_8))
            .put(payload.getBytes())
            .array()

        return ByteUtils.toHexString(HashUtils.doubleSha256(bytes))
    }

}
