package io.openfuture.chain.core.service.block

import io.openfuture.chain.core.annotation.OpenClass
import io.openfuture.chain.core.exception.ChainOutOfSyncException
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.payload.BlockPayload
import io.openfuture.chain.core.repository.BlockRepository
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.WalletService
import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8

@OpenClass
abstract class BaseBlockService<T : Block>(
    protected val repository: BlockRepository<T>,
    protected val blockService: BlockService,
    protected val walletService: WalletService,
    protected val delegateService: DelegateService
) {

    fun isPreviousBlockValid(previousBlock: Block, block: Block): Boolean {
        if (!isValidPreviousHash(block, previousBlock)) return false
        if (!isValidHeight(block, previousBlock)) return false
        if (!isValidTimeStamp(block, previousBlock)) return false
        if (previousBlock.height != 1L
            && !isValidSignature(previousBlock.hash, previousBlock.signature, previousBlock.publicKey)) return false
        return true
    }

    fun checkSync(block: Block) {
        val lastBlock = blockService.getLast()
        if (!isValidHeight(block, lastBlock) && isValidPreviousHash(block, lastBlock)) {
            throw ChainOutOfSyncException()
        }
    }

    protected fun save(block: T): T {
        return repository.save(block)
    }

    protected fun validateBase(block: Block) {
        val lastBlock = blockService.getLast()

        if (!isValidPreviousHash(block, lastBlock)) {
            throw ValidationException("Invalid block previous hash: ${block.previousHash}")
        }

        if (!isValidHeight(block, lastBlock)) {
            throw ValidationException("Invalid block height: ${block.height}")
        }

        if (!isValidTimeStamp(block, lastBlock)) {
            throw ValidationException("Invalid block timestamp: ${block.timestamp}")
        }

        if (!isValidHash(block)) {
            throw ValidationException("Invalid block hash: ${block.hash}")
        }

        if (!isValidSignature(block.hash, block.signature, block.publicKey)) {
            throw ValidationException("Invalid block signature: ${block.signature}")
        }
    }

    protected fun createHash(timestamp: Long, height: Long, previousHash: String, payload: BlockPayload): ByteArray {
        val bytes = ByteBuffer.allocate(LONG_BYTES + LONG_BYTES + previousHash.toByteArray(UTF_8).size + payload.getBytes().size)
            .putLong(timestamp)
            .putLong(height)
            .put(previousHash.toByteArray(UTF_8))
            .put(payload.getBytes())
            .array()

        return HashUtils.doubleSha256(bytes)
    }

    private fun isValidPreviousHash(block: Block, lastBlock: Block): Boolean = block.previousHash == lastBlock.hash

    private fun isValidTimeStamp(block: Block, lastBlock: Block): Boolean = block.timestamp > lastBlock.timestamp

    private fun isValidHeight(block: Block, lastBlock: Block): Boolean = block.height == lastBlock.height + 1

    private fun isValidHash(block: Block): Boolean {
        val hash = createHash(block.timestamp, block.height, block.previousHash, block.getPayload())
        return ByteUtils.toHexString(hash) == block.hash
    }

    private fun isValidSignature(hash: String, signature: String, publicKey: String): Boolean =
        SignatureUtils.verify(ByteUtils.fromHexString(hash), signature, ByteUtils.fromHexString(publicKey))

}