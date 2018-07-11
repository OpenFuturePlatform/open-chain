package io.openfuture.chain.service

import io.openfuture.chain.block.BlockValidationService
import io.openfuture.chain.block.SignatureCollector
import io.openfuture.chain.crypto.key.KeyHolder
import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.block.SignaturePublicKeyPair
import io.openfuture.chain.entity.*
import io.openfuture.chain.events.BlockCreationEvent
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.nio.converter.BlockSignaturesConverter
import io.openfuture.chain.protocol.CommunicationProtocol
import io.openfuture.chain.repository.BlockRepository
import io.openfuture.chain.util.BlockUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.EventListener
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultBlockService(
    private val blockRepository: BlockRepository<Block>,
    private val transactionService: TransactionService,
    private val signatureCollector: SignatureCollector,
    private val keyHolder: KeyHolder,
    private val signatureManager: SignatureManager,
    private val blockValidationService: BlockValidationService,
    private val blockSignaturesConverter: BlockSignaturesConverter,
    @Value("\${block.capacity}") private val transactionCapacity: Int,
    @Value("\${block.time.slot}") private val timeSlot: Long
) : BlockService {

    private var activeDelegates = emptyList<String>()

    @Transactional(readOnly = true)
    override fun get(id: Int): Block =
        blockRepository.getOne(id)
            ?: throw NotFoundException("Not found id $id")

    @Transactional(readOnly = true)
    override fun getAll(): MutableList<Block> = blockRepository.findAll()

    @Transactional(readOnly = true)
    override fun getLast(): Block =
        blockRepository.findFirstByOrderByHeightDesc()
            ?: throw NotFoundException("Last block not exist!")

    @Transactional(readOnly = true)
    override fun getLastGenesisBlock(): GenesisBlock =
        blockRepository.findFirstByVersionOrderByHeight(BlockVersion.GENESIS.version) as? GenesisBlock
            ?: throw NotFoundException("Last Genesis block not exist!")

    @Transactional
    override fun save(block: Block): Block {
        val savedBlock = blockRepository.save(block)


        return savedBlock
    }

    val taskScheduler = ThreadPoolTaskScheduler()

    override fun signBlock(block: Block): CommunicationProtocol.BlockSignatures {
        taskScheduler.scheduleAtFixedRate(
            {
                applyBlock()
            }, timeSlot)

        val signature = signatureManager.sign(HashUtils.hexStringToBytes(block.hash), keyHolder.getPrivateKey())

        val builder = CommunicationProtocol.BlockSignatures.newBuilder()
        blockSignaturesConverter.setBlockMessage(builder, block)
        val signatures = listOf<CommunicationProtocol.SignaturePublicKeyPair>(
            CommunicationProtocol.SignaturePublicKeyPair.newBuilder()
                .setSignature(signature)
                .setPublicKey(HashUtils.bytesToHexString(keyHolder.getPublicKey()))
                .build()
        )
        return builder
            .addAllSignatures(signatures)
            .build()
    }

    override fun signBlock(blockSignatures: CommunicationProtocol.BlockSignatures) {
        val block = blockSignaturesConverter.fromMessage(blockSignatures)

        val lastChainBlock = getLast()
        if (!blockValidationService.isValid(block, lastChainBlock)) {
            throw IllegalArgumentException("$block is not valid")
        }

        val signature = signatureManager.sign(HashUtils.hexStringToBytes(block.hash), keyHolder.getPrivateKey())
        val signaturePublicKeyPair = SignaturePublicKeyPair(
            signature,
            HashUtils.bytesToHexString(keyHolder.getPublicKey())
        )

        val signatures = blockSignatures.signaturesList
            .map { SignaturePublicKeyPair(it.signature, it.publicKey) }.toHashSet()
        signatures.add(signaturePublicKeyPair)
    }

    override fun addSignatures(blockSignature: CommunicationProtocol.BlockSignatures): Boolean {
        return signatureCollector.addBlockSignatures(blockSignature)
    }

    private fun create(transactions: List<Transaction>, previousBlock: Block): Block {
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
        signatureCollector.setPendingBlock(block)
        // broadcast will be here like this broadcastService.broadcastBlockToSign(block)
        return block
    }

    @EventListener
    fun fireBlockCreation(event: BlockCreationEvent) {
        val pendingTransactions = transactionService.getPendingTransactions()
        if (transactionCapacity == pendingTransactions.size) {
            val delegates = if (activeDelegates.isEmpty()) {
                getLastGenesisBlock().activeDelegateKeys
            } else {
                activeDelegates
            }.toList()
            val publicKey = HashUtils.bytesToHexString(keyHolder.getPublicKey())
            val previousBlock = getLast()
            if (publicKey == BlockUtils.getBlockProducer(delegates, previousBlock)) {
                this.create(pendingTransactions, previousBlock)
            }
        }
    }

    private fun applyBlock() {
        val blockSignatures = signatureCollector.getBlockSignatures()
        val genesisBlock = getLastGenesisBlock()

        if (blockSignatures.signaturesList.size.toDouble() / genesisBlock.activeDelegateKeys.size > 0.67) {
            val block = signatureCollector.getBlock()
            save(block)
        }
    }

}