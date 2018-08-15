package io.openfuture.chain.core.service.block

import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.repository.BlockRepository
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.WalletService
import io.openfuture.chain.core.util.BlockUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils

abstract class BaseBlockService<T : Block>(
    protected val repository: BlockRepository<T>,
    protected val blockService: BlockService,
    private val walletService: WalletService,
    protected val delegateService: DelegateService
) {

    protected fun save(block: T): T {
        updateBalanceByReward(block)
        return repository.save(block)
    }

    protected fun isValid(block: Block): Boolean {
        val lastBlock = blockService.getLast()

        return isValidPreviousHash(block, lastBlock)
            && isValidHeight(block, lastBlock)
            && isValidTimeStamp(block, lastBlock)
            && isValidHash(block)
            && isValidSignature(block.hash, block.signature, block.publicKey)
    }

    protected fun isSync(block: MainBlock): Boolean {
        val lastBlock = blockService.getLast()
        return isValidHeight(block, lastBlock)
    }

    private fun isValidPreviousHash(block: Block, lastBlock: Block): Boolean = block.previousHash == lastBlock.hash

    private fun isValidTimeStamp(block: Block, lastBlock: Block): Boolean = block.timestamp > lastBlock.timestamp

    private fun isValidHeight(block: Block, lastBlock: Block): Boolean = block.height == lastBlock.height + 1

    private fun isValidHash(block: Block): Boolean {
        val dataHash = BlockUtils.createHash(block.timestamp, block.height, block.previousHash, block.reward,
            block.getPayload())
        return ByteUtils.toHexString(dataHash) == block.hash
    }

    private fun isValidSignature(hash: String, signature: String, publicKey: String): Boolean =
        SignatureUtils.verify(ByteUtils.fromHexString(hash), signature, ByteUtils.fromHexString(publicKey))

    private fun updateBalanceByReward(block: Block) {
        val delegate = delegateService.getByPublicKey(block.publicKey)
        walletService.increaseBalance(delegate.address, block.reward)
    }

}