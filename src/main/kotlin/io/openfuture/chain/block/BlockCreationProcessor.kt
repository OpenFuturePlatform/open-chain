package io.openfuture.chain.block

import io.openfuture.chain.block.validation.BlockValidationProvider
import io.openfuture.chain.crypto.key.KeyHolder
import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.block.PendingBlock
import io.openfuture.chain.domain.block.SignaturePublicKeyPair
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.events.BlockCreationEvent
import io.openfuture.chain.service.BlockService
import io.openfuture.chain.util.BlockUtils
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class BlockCreationProcessor(
    private val blockService: BlockService,
    private val signatureCollector: SignatureCollector,
    private val keyHolder: KeyHolder,
    private val signatureManager: SignatureManager,
    private val blockValidationService: BlockValidationProvider
) {

    fun approveBlock(pendingBlock: PendingBlock): PendingBlock {
        val block = pendingBlock.block
        val lastChainBlock = blockService.getLast()
        if (!blockValidationService.isValid(block, lastChainBlock)) {
            throw IllegalArgumentException("Inbound block is not valid")
        }

        val hashAsBytes = HashUtils.hexStringToBytes(block.hash)
        val keyAsBytes = HashUtils.hexStringToBytes(pendingBlock.signature.publicKey)
        if (signatureManager.verify(hashAsBytes, pendingBlock.signature.signature, keyAsBytes)) {
            throw IllegalArgumentException("Inbound block's signature is invalid")
        }

        if(signatureCollector.addBlockSignature(pendingBlock)) {
            throw IllegalArgumentException("Either signature is already exists, or not related to pending block")
        }
        return signCreatedBlock(block)
    }

    @EventListener
    fun fireBlockCreation(event: BlockCreationEvent) {
        val publicKey = HashUtils.bytesToHexString(keyHolder.getPublicKey())
        val previousBlock = blockService.getLast()
        val genesisBlock = blockService.getLastGenesisBlock()
        if (publicKey == BlockUtils.getBlockProducer(genesisBlock.activeDelegateKeys, previousBlock)) {
            create(event.pendingTransactions, previousBlock)
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

    private fun create(transactions: List<Transaction>, previousBlock: Block) {
        val merkleRootHash = BlockUtils.calculateMerkleRoot(transactions)
        val time = System.currentTimeMillis()
        val hash = BlockUtils.calculateHash(previousBlock.hash, merkleRootHash, time, (previousBlock.height + 1))

        val privateKey = keyHolder.getPrivateKey()
        val signature = signatureManager.sign(hash, privateKey)
        val block = MainBlock(
            HashUtils.bytesToHexString(hash),
            previousBlock.height + 1,
            previousBlock.hash,
            merkleRootHash,
            time,
            signature,
            transactions
        )
        signCreatedBlock(block)
    }

}