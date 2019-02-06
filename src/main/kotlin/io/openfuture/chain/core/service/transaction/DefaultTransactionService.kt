package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.component.TransactionThroughput
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.TransactionService
import io.openfuture.chain.crypto.util.HashUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.ByteBuffer

@Service
class DefaultTransactionService(
    private val repository: TransactionRepository<Transaction>,
    private val uRepository: UTransactionRepository<UnconfirmedTransaction>,
    private val throughput: TransactionThroughput
) : TransactionService {

    @Transactional(readOnly = true)
    override fun getCount(): Long = repository.count()

    @Transactional(readOnly = true)
    override fun getUnconfirmedTransactionByHash(hash: String): UnconfirmedTransaction = uRepository.findOneByFooterHash(hash)
        ?: throw NotFoundException("Unconfirmed transaction with hash $hash not found")

    override fun getProducingPerSecond(): Long = throughput.getThroughput()

    override fun deleteBlockTransactions(blockHeights: List<Long>) {
        repository.deleteAllByBlockHeightIn(blockHeights)
        repository.flush()
    }

    override fun createHash(header: TransactionHeader, payload: TransactionPayload): String {
        val bytes = ByteBuffer.allocate(header.getBytes().size + payload.getBytes().size)
            .put(header.getBytes())
            .put(payload.getBytes())
            .array()

        return ByteUtils.toHexString(HashUtils.doubleSha256(bytes))
    }

}