package io.openfuture.chain.core.service.block

import io.openfuture.chain.core.component.BlockCapacityChecker
import io.openfuture.chain.core.model.entity.block.BaseBlock
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

abstract class BaseBlockService<T : Block>(
    protected val repository: BlockRepository<T>,
    protected val blockService: BlockService,
    private val walletService: WalletService,
    protected val delegateService: DelegateService,
    private val  capacityChecker: BlockCapacityChecker
) {

    protected fun save(block: T): T {
        updateBalanceByReward(block)
        capacityChecker.incrementCapacity(block.timestamp)
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

    protected fun isSync(block: Block): Boolean {
        val lastBlock = blockService.getLast()
        return isValidHeight(block, lastBlock) && isValidPreviousHash(block, lastBlock)
    }

    protected fun createHash(timestamp: Long, height: Long, previousHash: String, reward: Long, payload: BlockPayload): ByteArray {
        val bytes =  ByteBuffer.allocate(LONG_BYTES + LONG_BYTES + previousHash.toByteArray(UTF_8).size + LONG_BYTES + payload.getBytes().size)
            .putLong(timestamp).putLong(height)
            .put(previousHash.toByteArray(UTF_8))
            .putLong(reward)
            .put(payload.getBytes())
            .array()

        return HashUtils.doubleSha256(bytes)
    }

    private fun isValidPreviousHash(block: Block, lastBlock: Block): Boolean = block.previousHash == lastBlock.hash

    private fun isValidTimeStamp(block: Block, lastBlock: Block): Boolean = block.timestamp > lastBlock.timestamp

    private fun isValidHeight(block: Block, lastBlock: Block): Boolean = block.height == lastBlock.height + 1

    private fun isValidHash(block: Block): Boolean {
        val hash = createHash(block.timestamp, block.height, block.previousHash, block.reward,
            block.getPayload())
        return ByteUtils.toHexString(hash) == block.hash
    }

    private fun isValidSignature(hash: String, signature: String, publicKey: String): Boolean =
        SignatureUtils.verify(ByteUtils.fromHexString(hash), signature, ByteUtils.fromHexString(publicKey))

    private fun updateBalanceByReward(block: Block) {
        val delegate = delegateService.getByPublicKey(block.publicKey)
        walletService.increaseBalance(delegate.address, block.reward)
    }

}