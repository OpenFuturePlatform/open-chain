package io.openfuture.chain.core.service.block

import io.openfuture.chain.core.model.entity.block.BaseBlock
import io.openfuture.chain.core.repository.BlockRepository
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.WalletService
import io.openfuture.chain.core.util.BlockUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.beans.factory.annotation.Autowired

abstract class BaseBlockService<T : BaseBlock>(
    protected val repository: BlockRepository<T>,
    protected val blockService: BlockService
) {

    @Autowired
    private lateinit var walletService: WalletService

    @Autowired
    private lateinit var delegateService: DelegateService


    protected fun save(block: T): T {
        updateBalanceByReward(block)
        return repository.save(block)
    }

    protected fun isValid(block: BaseBlock): Boolean {
        val lastBlock = blockService.getLast()

        return isValidPreviousHash(block, lastBlock)
            && isValidHeight(block, lastBlock)
            && isValidTimeStamp(block, lastBlock)
            && isValidHash(block)
            && isValidSignature(block.hash, block.signature, block.publicKey)
    }

    private fun isValidPreviousHash(block: BaseBlock, lastBlock: BaseBlock): Boolean = block.previousHash == lastBlock.hash

    private fun isValidTimeStamp(block: BaseBlock, lastBlock: BaseBlock): Boolean = block.timestamp > lastBlock.timestamp

    private fun isValidHeight(block: BaseBlock, lastBlock: BaseBlock): Boolean = block.height == lastBlock.height + 1

    private fun isValidHash(block: BaseBlock): Boolean {
        val dataHash = BlockUtils.createHash(block.timestamp, block.height, block.previousHash, block.reward,
            block.getPayload())
        return ByteUtils.toHexString(dataHash) == block.hash
    }

    private fun isValidSignature(hash: String, signature: String, publicKey: String): Boolean =
        SignatureUtils.verify(ByteUtils.fromHexString(hash), signature, ByteUtils.fromHexString(publicKey))

    private fun updateBalanceByReward(block: BaseBlock) {
        val delegate = delegateService.getByPublicKey(block.publicKey)
        walletService.increaseBalance(delegate.address, block.reward)
    }

}