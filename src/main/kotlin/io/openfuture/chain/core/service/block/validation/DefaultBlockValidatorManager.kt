package io.openfuture.chain.core.service.block.validation

import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.repository.BlockRepository
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
    private val repository: BlockRepository<Block>,
    private val mainBlockValidator: MainBlockValidator
) : BlockValidatorManager {

    companion object {
        private val log = LoggerFactory.getLogger(DefaultBlockValidatorManager::class.java)
    }


    override fun verify(block: Block): Boolean {
        return try {
            validate(block)
            true
        } catch (e: ValidationException) {
            log.warn(e.message)
            false
        }
    }

    private fun validate(block: Block) {
        val lastBlock = repository.findFirstByOrderByHeightDesc()

        checkSignature(block)
        checkHash(block)
        checkTimeStamp(block, lastBlock)
        checkHeight(block, lastBlock)
        checkPreviousHash(block, lastBlock)

        when (block) {
            is MainBlock -> mainBlockValidator.validate(block)
        }
    }

    private fun checkSignature(block: Block) {
        if (!SignatureUtils.verify(fromHexString(block.hash), block.signature, fromHexString(block.publicKey))) {
            throw ValidationException("Incorrect signature")
        }
    }

    private fun checkHash(block: Block) {
        if (block.hash != ByteUtils.toHexString(HashUtils.doubleSha256(block.getBytes()))) {
            throw ValidationException("Incorrect hash")
        }
    }

    private fun checkTimeStamp(block: Block, lastBlock: Block) {
        if (block.timestamp <= lastBlock.timestamp) {
            throw ValidationException("Incorrect timestamp")
        }
    }

    private fun checkHeight(block: Block, lastBlock: Block) {
        if (block.height != lastBlock.height + 1) {
            throw ValidationException("Incorrect height")
        }
    }

    private fun checkPreviousHash(block: Block, lastBlock: Block) {
        if (block.previousHash != lastBlock.hash) {
            throw ValidationException("Incorrect previous hash")
        }
    }

}