package io.openfuture.chain.core.service.receipt

import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.repository.ReceiptRepository
import io.openfuture.chain.core.service.ReceiptService
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.crypto.util.HashUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultReceiptService(
    private val repository: ReceiptRepository
) : ReceiptService {

    override fun getByTransactionHash(hash: String): Receipt = repository.findOneByTransactionHash(hash)
        ?: throw NotFoundException("Receipt for transaction $hash not found")

    override fun getAllByBlock(block: Block): List<Receipt> = repository.findAllByBlock(block)

    @Transactional
    override fun commit(receipt: Receipt) {
        BlockchainLock.writeLock.lock()
        try {
            repository.save(receipt)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

    override fun verify(receipt: Receipt): Boolean =
        receipt.hash == ByteUtils.toHexString(HashUtils.doubleSha256(receipt.getBytes()))

    @Transactional
    override fun deleteBlockReceipts(blockHeights: List<Long>) {
        BlockchainLock.writeLock.lock()
        try {
            repository.deleteAllByBlockHeightIn(blockHeights)
            repository.flush()
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

}