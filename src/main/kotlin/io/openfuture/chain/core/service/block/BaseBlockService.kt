package io.openfuture.chain.core.service.block

import io.openfuture.chain.core.model.entity.block.BaseBlock
import io.openfuture.chain.core.model.entity.block.payload.BlockPayload
import io.openfuture.chain.core.repository.BlockRepository
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8

abstract class BaseBlockService<T : BaseBlock>(
    protected val repository: BlockRepository<T>,
    protected val blockService: BlockService,
    protected val delegateService: DelegateService
) {

    protected fun isValid(block: BaseBlock): Boolean {
        val lastBlock = blockService.getLast()

        return isValidPreviousHash(block, lastBlock)
            && isValidHeight(block, lastBlock)
            && isValidTimeStamp(block, lastBlock)
            && isValidHash(block)
            && isValidSignature(block.hash, block.signature, block.publicKey)
    }

    protected fun createHash(timestamp: Long, height: Long, previousHash: String, payload: BlockPayload): ByteArray {
        val bytes =  ByteBuffer.allocate(LONG_BYTES + LONG_BYTES + previousHash.toByteArray(UTF_8).size + payload.getBytes().size)
            .putLong(timestamp).putLong(height)
            .put(previousHash.toByteArray(UTF_8))
            .put(payload.getBytes())
            .array()

        return HashUtils.doubleSha256(bytes)
    }

    private fun isValidPreviousHash(block: BaseBlock, lastBlock: BaseBlock): Boolean = block.previousHash == lastBlock.hash

    private fun isValidTimeStamp(block: BaseBlock, lastBlock: BaseBlock): Boolean = block.timestamp > lastBlock.timestamp

    private fun isValidHeight(block: BaseBlock, lastBlock: BaseBlock): Boolean = block.height == lastBlock.height + 1

    private fun isValidHash(block: BaseBlock): Boolean {
        val hash = createHash(block.timestamp, block.height, block.previousHash, block.getPayload())
        return ByteUtils.toHexString(hash) == block.hash
    }

    private fun isValidSignature(hash: String, signature: String, publicKey: String): Boolean =
        SignatureUtils.verify(ByteUtils.fromHexString(hash), signature, ByteUtils.fromHexString(publicKey))

}