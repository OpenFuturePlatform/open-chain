package io.openfuture.chain.core.service.block.validation

import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.service.BlockValidatorManager
import io.openfuture.chain.core.service.MainBlockValidator
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils.fromHexString
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DefaultBlockValidatorManager(
    private val mainBlockValidator: MainBlockValidator
) : BlockValidatorManager {

    companion object {
        private val log = LoggerFactory.getLogger(DefaultBlockValidatorManager::class.java)
    }


    override fun verify(block: Block, lastBlock: Block, new: Boolean): Boolean {
        return try {
            validate(block, lastBlock, new)
            true
        } catch (e: ValidationException) {
            log.warn(e.message)
            false
        }
    }

    private fun validate(block: Block, lastBlock: Block, new: Boolean) {
        checkSignature(block)
        checkHash(block)
        checkTimeStamp(block, lastBlock)
        checkHeight(block, lastBlock)
        checkPreviousHash(block, lastBlock)

        when (block) {
            is MainBlock -> mainBlockValidator.validate(block, new)
        }
    }

    private fun checkSignature(block: Block) {
        if (!SignatureUtils.verify(fromHexString(block.hash), block.signature, fromHexString(block.publicKey))) {
            throw ValidationException("Incorrect signature in block: height #${block.height}, hash ${block.hash}\"")
        }
    }

    private fun checkHash(block: Block) {
        if (block.hash != ByteUtils.toHexString(HashUtils.doubleSha256(block.getBytes()))) {
            throw ValidationException("Incorrect hash in block: height #${block.height}, hash ${block.hash}")
        }
    }

    private fun checkTimeStamp(block: Block, lastBlock: Block) {
        if (block.timestamp <= lastBlock.timestamp) {
            throw ValidationException("Incorrect timestamp in block: height #${block.height}, hash ${block.hash}")
        }
    }

    private fun checkHeight(block: Block, lastBlock: Block) {
        if (block.height != lastBlock.height + 1) {
            throw ValidationException("Incorrect height in block: height #${block.height}, hash ${block.hash}")
        }
    }

    private fun checkPreviousHash(block: Block, lastBlock: Block) {
        if (block.previousHash != lastBlock.hash) {
            throw ValidationException("Incorrect previous hash in block: height #${block.height}, hash ${block.hash}")
        }
    }

}