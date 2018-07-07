package io.openfuture.chain.component.initializer

import com.fasterxml.jackson.databind.ObjectMapper
import io.openfuture.chain.nio.client.handler.ConnectionClientHandler
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.block.MainBlockDto
import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.domain.transaction.vote.VoteDto
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.service.BlockService
import io.openfuture.chain.service.TransactionService
import io.openfuture.chain.util.BlockUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import org.springframework.scheduling.annotation.Scheduled


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
        if (0 == blockService.getAll().size) {
            blockService.add(createGenesisBlock())
        }

        log.info("Block chain successfully initialized")
    }

    @Scheduled(fixedDelayString = "10000")
    fun createTransactionSchedule() {
        val trx = transactionService.add(createRandomTransaction())
        log.info("Created new transaction {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(trx))
    }

    @Scheduled(fixedDelayString = "60000")
    fun createBlockSchedule() {
        val transactions = transactionService.getAllPending()
        val block = blockService.add(createBlock(transactions))
        log.info("Created new block {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(block))
    }

    // todo temp solution
    private fun createGenesisBlock(): MainBlockDto {
        val previousHash = HashUtils.generateHash("previousHash".toByteArray())
        val transactions = mutableSetOf(createRandomTransaction())
        val merkleRoot = BlockUtils.calculateMerkleRoot(transactions)
        val hash = HashUtils.bytesToHexString(BlockUtils.calculateHash(previousHash, merkleRoot, 0, 0))

        return MainBlockDto(hash, 0, previousHash, merkleRoot, 0,
                "genesis_signature", transactions)
    }

    @Deprecated("generate random transaction")
    private fun createRandomTransaction(): TransactionDto {
        val amount = Math.round(Math.random())
        val data = VoteTransactionData(amount, "recipientKey", "senderKey", "senderSignature",
                mutableListOf(VoteDto("publicKey_X", 100)))
        return transactionService.createVote(data)
    }

    @Deprecated("generate block")
    private fun createBlock(transactions: MutableSet<TransactionDto>): MainBlockDto {
        return blockService.create(transactions)
    }

}