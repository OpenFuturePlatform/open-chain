package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.TransactionService
import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import io.openfuture.chain.crypto.util.HashUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8

@Service
class DefaultTransactionService(
    private val repository: TransactionRepository<Transaction>,
    private val uRepository: UTransactionRepository<UnconfirmedTransaction>
) : TransactionService {

    @Transactional(readOnly = true)
    override fun getAllUnconfirmedByAddress(address: String): List<UnconfirmedTransaction> =
        uRepository.findAllBySenderAddress(address)

    @Transactional(readOnly = true)
    override fun getCount(): Long = repository.count()


    @Transactional(readOnly = true)
    override fun getUTransactionByHash(hash: String): UnconfirmedTransaction = uRepository.findOneByHash(hash)
        ?: throw NotFoundException("Unconfirmed transaction with hash $hash not found")

    override fun createHash(timestamp: Long, fee: Long, senderAddress: String, payload: TransactionPayload): String {
        val bytes = ByteBuffer.allocate(LONG_BYTES + LONG_BYTES +
            senderAddress.toByteArray(UTF_8).size + payload.getBytes().size)
            .putLong(timestamp)
            .putLong(fee)
            .put(senderAddress.toByteArray(UTF_8))
            .put(payload.getBytes())
            .array()

        return ByteUtils.toHexString(HashUtils.doubleSha256(bytes))
    }

}