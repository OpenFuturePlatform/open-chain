package io.openfuture.chain.component.initializer

import com.fasterxml.jackson.databind.ObjectMapper
import io.openfuture.chain.domain.block.BlockDto
import io.openfuture.chain.domain.block.nested.BlockData
import io.openfuture.chain.domain.block.nested.BlockHash
import io.openfuture.chain.domain.block.nested.MerkleHash
import io.openfuture.chain.nio.client.handler.ConnectionClientHandler
import io.openfuture.chain.util.BlockUtils
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.transaction.TransactionData
import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.service.BlockService
import io.openfuture.chain.service.TransactionService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import org.springframework.scheduling.annotation.Scheduled
import java.util.*


@Component
class BlockchainInitializer(
        private val blockService: BlockService,
        private val transactionService: TransactionService,
        private val objectMapper: ObjectMapper
) {

    companion object {
        private val log = LoggerFactory.getLogger(ConnectionClientHandler::class.java)
    }

    @PostConstruct
    fun initBlockChain() {
        if (0L == blockService.chainSize()) {
            blockService.add(createGenesisBlock())
        }

        log.info("Block chain successfully initialized")
    }

    @Scheduled(fixedDelayString = "10000")
    fun createTransactionSchedule() {
        val trx = TransactionDto(transactionService.add(createRandomTransaction()))
        log.info("Created new transaction {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(trx))
    }

    @Scheduled(fixedDelayString = "60000")
    fun createBlockSchedule() {
        val block = BlockDto(blockService.add(createBlock()))
        log.info("Created new block {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(block))
    }

    // todo temp solution
    private fun createGenesisBlock(): BlockDto {
        val merkleHash = MerkleHash(HashUtils.generateHash("merkleHash".toByteArray()), listOf())
        val previousHash = HashUtils.generateHash("previousHash".toByteArray())
        val blockData = BlockData(0, 0, previousHash, merkleHash)
        val blockHash = BlockHash(0, BlockUtils.generateHash(blockData, 0))
        val signature = BlockUtils.generateSignature("genesisPrivateKey", blockData, blockHash)
        return BlockDto(blockData, blockHash, "genesisPublicKey", signature)
    }

    @Deprecated("generate random transaction")
    private fun createRandomTransaction(): TransactionDto {
        val amount = Random().nextLong()
        val timestamp = Date().time
        val data = TransactionData(amount, timestamp, "recipientKey", "senderKey", "signature")
        return transactionService.create(data)
    }

    @Deprecated("generate block")
    private fun createBlock(): BlockDto {
        return blockService.create("privateKey", "publicKey", 2)
    }

}