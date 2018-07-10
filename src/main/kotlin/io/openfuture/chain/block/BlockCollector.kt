package io.openfuture.chain.block

import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.*
import io.openfuture.chain.protocol.CommunicationProtocol
import org.springframework.stereotype.Component
import java.util.concurrent.locks.ReentrantReadWriteLock

@Component
class BlockCollector(
    private val blockValidationService: BlockValidationService,
    private val signatureManager: SignatureManager
) {

    private val lock = ReentrantReadWriteLock()

    private var signedBlocks = mutableListOf<CommunicationProtocol.SignedBlock>()

    // variable to collect the blocks from the same round only
    private lateinit var blockCollectionHash: String


    fun setBlockCollectionHash(blockCollectionHash: String) {
        CommunicationProtocol.Packet.BodyCase.TIME_SYNC_REQUEST
        try {
            lock.writeLock().lock()
            this.blockCollectionHash = blockCollectionHash
            signedBlocks = mutableListOf()
        } finally {
            lock.writeLock().unlock()
        }
    }

    fun addBlock(signedBlock: CommunicationProtocol.SignedBlock) {
        try {
            lock.writeLock().lock()
            if (signedBlock.mainBlock.hash == blockCollectionHash) {
                signedBlocks.add(signedBlock)
            }
        } finally {
            lock.writeLock().unlock()
        }
    }

    fun mergeBlockSigns(): CommunicationProtocol.FullSignedBlock {
        try {
            lock.readLock().lock()

            val firstSignedBlock = signedBlocks.first()
            val firstBlock = toBlock(firstSignedBlock)
            for (signedBlock in signedBlocks) {
                val block = toBlock(signedBlock)

                val blockIsValid = blockValidationService.isValid(block)
                if (!blockIsValid || block.hash != firstBlock.hash) {
                    throw IllegalArgumentException("$signedBlocks has wrong block = $signedBlock")
                }

                val hash = HashUtils.hexStringToBytes(block.hash)
                val publicKey = HashUtils.hexStringToBytes(signedBlock.publicKey)
                signatureManager.verify(hash, signedBlock.signature, publicKey)
            }

            val signatures = signedBlocks.map { it.signature }.toSet()

            return setBlockProto(CommunicationProtocol.FullSignedBlock.newBuilder(), firstBlock)
                .addAllSignatures(signatures)
                .build()
        } finally {
            lock.readLock().unlock()
        }
    }

    private fun setBlockProto(
            builder: CommunicationProtocol.FullSignedBlock.Builder,
            block: Block): CommunicationProtocol.FullSignedBlock.Builder {
        if (block.version == BlockVersion.MAIN.version) {
            builder.mainBlock = toMainBlockProto(block)
        } else if (block.version == BlockVersion.GENESIS.version) {
            builder.genesisBlock = toGenesisBlockProto(block)
        }
        return builder
    }

    private fun toBlock(signedBlock: CommunicationProtocol.SignedBlock): Block {
        var block: Block? = null
        if (signedBlock.mainBlock != null) {
            block = toMainBlock(signedBlock)
        } else if (signedBlock.genesisBlock != null) {
            block = toGenesisBlock(signedBlock)
        } else {
            throw IllegalArgumentException("signedBlock has wrong block")
        }

        return block
    }

    private fun toMainBlock(signedBlock: CommunicationProtocol.SignedBlock): MainBlock {
        val mainBlock = signedBlock.mainBlock
        return MainBlock(
            mainBlock.hash,
            mainBlock.height,
            mainBlock.previousHash,
            mainBlock.merkleHash,
            mainBlock.timestamp,
            mainBlock.signature,
            mainBlock.transactionsList.map { toTransaction(it) }.toList())
    }

    private fun toGenesisBlock(signedBlock: CommunicationProtocol.SignedBlock): GenesisBlock {
        val genesisBlock = signedBlock.genesisBlock
        return GenesisBlock(
            genesisBlock.hash,
            genesisBlock.height,
            genesisBlock.previousHash,
            genesisBlock.merkleHash,
            genesisBlock.timestamp,
            genesisBlock.epochIndex,
            genesisBlock.activeDelegateIpsList.toSet())
    }

    private fun toTransaction(transaction: CommunicationProtocol.Transaction): Transaction {
        return Transaction(
            transaction.hash,
            transaction.amount,
            transaction.timestamp,
            transaction.recipientkey,
            transaction.senderKey,
            transaction.signature
        )
    }

    private fun toMainBlockProto(block: Block): CommunicationProtocol.MainBlock {
        val mainBlock = block as MainBlock
        return CommunicationProtocol.MainBlock.newBuilder()
            .setHash(mainBlock.hash)
            .setHeight(mainBlock.height)
            .setPreviousHash(mainBlock.previousHash)
            .setMerkleHash(mainBlock.merkleHash)
            .setTimestamp(mainBlock.timestamp)
            .setSignature(mainBlock.signature)
            .addAllTransactions(mainBlock.transactions.map { toTransactionProto(it) }.toList())
            .build()
    }

    private fun toGenesisBlockProto(block: Block): CommunicationProtocol.GenesisBlock {
        val genesisBlock = block as GenesisBlock
        return CommunicationProtocol.GenesisBlock.newBuilder()
            .setHash(genesisBlock.hash)
            .setHeight(genesisBlock.height)
            .setPreviousHash(genesisBlock.previousHash)
            .setMerkleHash(genesisBlock.merkleHash)
            .setTimestamp(genesisBlock.timestamp)
            .setEpochIndex(genesisBlock.epochIndex)
            .addAllActiveDelegateIps(genesisBlock.activeDelegateIps.toList())
            .build()
    }

    private fun toTransactionProto(transaction: Transaction): CommunicationProtocol.Transaction {
        return CommunicationProtocol.Transaction.newBuilder()
            .setHash(transaction.hash)
            .setAmount(transaction.amount)
            .setTimestamp(transaction.timestamp)
            .setRecipientkey(transaction.recipientkey)
            .setSenderKey(transaction.senderKey)
            .setSignature(transaction.signature)
            .build()
    }

}