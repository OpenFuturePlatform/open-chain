package io.openfuture.chain.core.service.block.validation

import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.service.block.validation.pipeline.BlockValidationPipeline
import io.openfuture.chain.core.util.BlockValidateHandler
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils.fromHexString
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils.toHexString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
abstract class BlockPipelineValidator {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(BlockPipelineValidator::class.java)
    }


    fun verify(block: Block, lastBlock: Block, new: Boolean, blockValidationPipeline: BlockValidationPipeline): Boolean {
        return try {
            validate(block, lastBlock, new, blockValidationPipeline)
            true
        } catch (e: ValidationException) {
            log.warn(e.message)
            false
        }
    }

    fun validate(block: Block, lastBlock: Block, new: Boolean, blockValidationPipeline: BlockValidationPipeline) {
        blockValidationPipeline.invoke(block, lastBlock, new)
    }

    fun checkSignature(): BlockValidateHandler = { block, _, _ ->
        if (!SignatureUtils.verify(fromHexString(block.hash), block.signature, fromHexString(block.publicKey))) {
            throw ValidationException("Incorrect signature in block: height #${block.height}, hash ${block.hash}")
        }
    }

    fun checkHash(): BlockValidateHandler = { block, _, _ ->
        if (block.hash != toHexString(HashUtils.doubleSha256(block.getBytes()))) {
            throw ValidationException("Incorrect hash in block: height #${block.height}, hash ${block.hash}")
        }
    }

    fun checkTimeStamp(): BlockValidateHandler = { block, lastBlock, _ ->
        if (block.timestamp <= lastBlock.timestamp) {
            throw ValidationException("Incorrect timestamp in block: height #${block.height}, hash ${block.hash}")
        }
    }

    fun checkHeight(): BlockValidateHandler = { block, lastBlock, _ ->
        if (block.height != lastBlock.height + 1) {
            throw ValidationException("Incorrect height in block: height #${block.height}, hash ${block.hash}")
        }
    }

    fun checkPreviousHash(): BlockValidateHandler = { block, lastBlock, _ ->
        if (block.previousHash != lastBlock.hash) {
            throw ValidationException("Incorrect previous hash in block: height #${block.height}, hash ${block.hash}")
        }
    }

}