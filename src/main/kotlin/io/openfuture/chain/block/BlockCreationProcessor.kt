package io.openfuture.chain.block

import io.openfuture.chain.block.validation.BlockValidationProvider
import io.openfuture.chain.crypto.key.NodeKeyHolder
import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.block.BlockCreationEvent
import io.openfuture.chain.domain.block.PendingBlock
import io.openfuture.chain.domain.block.SignaturePublicKeyPair
import io.openfuture.chain.entity.*
import io.openfuture.chain.service.BlockService
import io.openfuture.chain.service.ConsensusService
import io.openfuture.chain.util.BlockUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class BlockCreationProcessor(
    private val blockService: BlockService,
    private val signatureCollector: SignatureCollector,
    private val keyHolder: NodeKeyHolder,
    private val signatureManager: SignatureManager,
    private val blockValidationService: BlockValidationProvider,
    private val consensusService: ConsensusService
) {

    fun approveBlock(pendingBlock: PendingBlock): PendingBlock {
        val block = pendingBlock.block

        if (!blockValidationService.isValid(block)) {
            throw IllegalArgumentException("Inbound block is not valid")
        }

        val hashAsBytes = HashUtils.hexStringToBytes(block.hash)
        val keyAsBytes = HashUtils.hexStringToBytes(pendingBlock.signature.publicKey)
        if (!signatureManager.verify(hashAsBytes, pendingBlock.signature.signature, keyAsBytes)) {
            throw IllegalArgumentException("Inbound block's signature is invalid")
        }

        if(!signatureCollector.addBlockSignature(pendingBlock)) {
            throw IllegalArgumentException("Either signature is already exists, or not related to pending block")
        }
        return signCreatedBlock(block)
    }

    @EventListener
    fun fireBlockCreation(event: BlockCreationEvent) {
        val publicKey = HashUtils.bytesToHexString(keyHolder.getPublicKey())
        val previousBlock = blockService.getLastMain()
        val genesisBlock = blockService.getLastGenesis()
        if (publicKey == BlockUtils.getBlockProducer(genesisBlock.activeDelegateKeys, previousBlock)) {
            create(event.pendingTransactions, previousBlock, genesisBlock)
        }
    }

    private fun signCreatedBlock(block: Block): PendingBlock {
        val signature = signatureManager.sign(HashUtils.hexStringToBytes(block.hash), keyHolder.getPrivateKey())
        val publicKey = HashUtils.bytesToHexString(keyHolder.getPublicKey())
        val signaturePublicKeyPair = SignaturePublicKeyPair(signature, publicKey)
        val pendingBlock = PendingBlock(block, signaturePublicKeyPair)
        signatureCollector.setPendingBlock(pendingBlock)
        return pendingBlock
    }

    private fun create(transactions: List<Transaction>, previousBlock: Block, genesisBlock: GenesisBlock) {
        val blockType = if (consensusService.isGenesisBlockNeeded()) BlockType.GENESIS else BlockType.MAIN

        val time = System.currentTimeMillis()
        val privateKey = keyHolder.getPrivateKey()
        val block = when(blockType) {
            BlockType.MAIN -> {
                val merkleRootHash = BlockUtils.calculateMerkleRoot(transactions)
                val hash = BlockUtils.calculateHash(previousBlock.hash, time, (previousBlock.height + 1), merkleRootHash)
                val signature = signatureManager.sign(hash, privateKey)
                MainBlock(
                    HashUtils.bytesToHexString(hash),
                    previousBlock.height + 1,
                    previousBlock.hash,
                    merkleRootHash,
                    time,
                    signature,
                    transactions
                )
            }
            BlockType.GENESIS -> {
                val hash = BlockUtils.calculateHash(previousBlock.hash, time, (previousBlock.height + 1))
                val signature = signatureManager.sign(hash, privateKey)
                GenesisBlock(
                    HashUtils.bytesToHexString(hash),
                    previousBlock.height + 1,
                    previousBlock.hash,
                    StringUtils.EMPTY,
                    time,
                    signature,
                    genesisBlock.epochIndex + 1,
                    emptySet()  // place active delegates
                )
            }
        }
        signCreatedBlock(block)
    }

}