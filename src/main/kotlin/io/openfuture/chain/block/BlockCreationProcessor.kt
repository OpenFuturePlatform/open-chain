package io.openfuture.chain.block

import io.openfuture.chain.block.validation.BlockValidationProvider
import io.openfuture.chain.crypto.key.NodeKeyHolder
import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.domain.block.BlockCreationEvent
import io.openfuture.chain.domain.block.PendingBlock
import io.openfuture.chain.domain.block.SignaturePublicKeyPair
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.BlockType
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.service.BlockService
import io.openfuture.chain.service.ConsensusService
import io.openfuture.chain.util.BlockUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class BlockCreationProcessor(
    private val blockService: BlockService,
    private val signatureCollector: SignatureCollector,
    private val keyHolder: NodeKeyHolder,
    private val blockValidationService: BlockValidationProvider,
    private val consensusService: ConsensusService
) {

    fun approveBlock(pendingBlock: PendingBlock): PendingBlock {
        val block = pendingBlock.block

        if (!blockValidationService.isValid(block)) {
            throw IllegalArgumentException("Inbound block is not valid")
        }

        val hashAsBytes = ByteUtils.fromHexString(block.hash)
        val keyAsBytes = ByteUtils.fromHexString(pendingBlock.signature.publicKey)
        if (!SignatureManager.verify(hashAsBytes, pendingBlock.signature.signature, keyAsBytes)) {
            throw IllegalArgumentException("Inbound block's signature is invalid")
        }

        if(!signatureCollector.addBlockSignature(pendingBlock)) {
            throw IllegalArgumentException("Either signature is already exists, or not related to pending block")
        }
        return signCreatedBlock(block)
    }

    @EventListener
    fun fireBlockCreation(event: BlockCreationEvent) {
        val publicKey = ByteUtils.toHexString(keyHolder.getPublicKey())
        val previousBlock = blockService.getLastMain()
        val genesisBlock = blockService.getLastGenesis()
        if (publicKey == BlockUtils.getBlockProducer(genesisBlock.activeDelegateKeys, previousBlock)) {
            create(event.pendingTransactions, previousBlock, genesisBlock)
        }
    }

    private fun signCreatedBlock(block: Block): PendingBlock {
        val signature = SignatureManager.sign(ByteUtils.fromHexString(block.hash), keyHolder.getPrivateKey())
        val publicKey = ByteUtils.toHexString(keyHolder.getPublicKey())
        val signaturePublicKeyPair = SignaturePublicKeyPair(signature, publicKey)
        val pendingBlock = PendingBlock(block, signaturePublicKeyPair)
        signatureCollector.setPendingBlock(pendingBlock)
        return pendingBlock
    }

    private fun create(transactions: MutableList<BaseTransaction>, previousBlock: Block, genesisBlock: GenesisBlock) {
        val blockType = if (consensusService.isGenesisBlockNeeded()) BlockType.GENESIS else BlockType.MAIN

        val time = System.currentTimeMillis()
        val privateKey = keyHolder.getPrivateKey()
        val block = when(blockType) {
            BlockType.MAIN -> {
                val mainBlock = MainBlock(
                    privateKey,
                    previousBlock.height + 1,
                    previousBlock.hash,
                    BlockUtils.calculateMerkleRoot(transactions),
                    time,
                    transactions
                )
                mainBlock
            }
            BlockType.GENESIS -> {
                val genBlock = GenesisBlock(
                    privateKey,
                    previousBlock.height + 1,
                    previousBlock.hash,
                    time,
                    genesisBlock.epochIndex + 1,
                    emptySet()  // place active delegates
                )
                genBlock
            }
        }
        signCreatedBlock(block)
    }

}