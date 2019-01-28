package io.openfuture.chain.core.service.block

import io.openfuture.chain.core.annotation.OpenClass
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.repository.BlockRepository
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.DelegateStateService
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils

@OpenClass
abstract class BaseBlockService<T : Block>(
    protected val repository: BlockRepository<T>,
    protected val blockService: BlockService,
    protected val delegateStateService: DelegateStateService
) {

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


    private fun isValidPreviousHash(block: Block, lastBlock: Block): Boolean = block.previousHash == lastBlock.hash

    private fun isValidTimeStamp(block: Block, lastBlock: Block): Boolean = block.timestamp > lastBlock.timestamp

    private fun isValidHeight(block: Block, lastBlock: Block): Boolean = block.height == lastBlock.height + 1

    private fun isValidHash(block: Block): Boolean {
        val hash = blockService.createHash(block.timestamp, block.height, block.previousHash, block.getPayload())
        return ByteUtils.toHexString(hash) == block.hash
    }

    private fun isValidSignature(hash: String, signature: String, publicKey: String): Boolean =
        SignatureUtils.verify(ByteUtils.fromHexString(hash), signature, ByteUtils.fromHexString(publicKey))

}