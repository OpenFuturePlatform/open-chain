package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.model.entity.transaction.BaseTransaction
import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
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
    @Autowired protected lateinit var walletService: WalletService
    @Autowired protected lateinit var baseService: TransactionService
    @Autowired protected lateinit var transactionService: TransactionService
    @Autowired private lateinit var cryptoService: CryptoService


    open fun save(utx: U): U {
        return unconfirmedRepository.save(utx)
    }

    open fun save(tx: T): T {
        updateBalanceByFee(tx)
        return repository.save(tx)
    }

    protected fun confirm(utx: U, tx: T): T {
        unconfirmedRepository.delete(utx)
        return save(tx)
    }

    protected fun validateBase(utx: BaseTransaction) {
        if (!isValidAddress(utx.header.senderAddress, utx.senderPublicKey)) {
            throw ValidationException("Invalid transaction address: ${utx.header.senderAddress}")
        }

        if (!isValidHash(utx.header, utx.getPayload(), utx.hash)) {
            throw ValidationException("Invalid transaction hash: ${utx.hash}")
        }

        if (!isValidSignature(utx.hash, utx.senderSignature, utx.senderPublicKey)) {
            throw ValidationException("Invalid transaction signature: ${utx.senderSignature}")
        }
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

    private fun isValidHash(header: TransactionHeader, payload: TransactionPayload, hash: String): Boolean {
        return createHash(header, payload) == hash
    }

    private fun isValidSignature(hash: String, signature: String, publicKey: String): Boolean {
        return SignatureUtils.verify(ByteUtils.fromHexString(hash), signature, ByteUtils.fromHexString(publicKey))
    }

    private fun createHash(header: TransactionHeader, payload: TransactionPayload): String {
        val bytes = ByteBuffer.allocate(LONG_BYTES + LONG_BYTES + header.senderAddress.toByteArray(UTF_8).size + payload.getBytes().size)
            .put(header.getBytes())
            .put(payload.getBytes())
            .array()

        return ByteUtils.toHexString(HashUtils.doubleSha256(bytes))
    }

}