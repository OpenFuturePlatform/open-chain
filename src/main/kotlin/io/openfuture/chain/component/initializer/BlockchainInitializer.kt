package io.openfuture.chain.component.initializer

import com.fasterxml.jackson.databind.ObjectMapper
import io.openfuture.chain.nio.client.handler.ConnectionClientHandler
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.block.MainBlockDto
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.TransferTransactionDto
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.Stakeholder
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.entity.peer.Delegate
import io.openfuture.chain.service.*
import io.openfuture.chain.util.BlockUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.util.CollectionUtils
import java.util.*

@Component
class BlockchainInitializer(
    private val blockService: BlockService,
    private val stakeholderService: StakeholderService,
    private val delegateService: DelegateService,
    private val transferTransactionService: TransferTransactionService,
    private val voteTransactionService: VoteTransactionService,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private val log = LoggerFactory.getLogger(ConnectionClientHandler::class.java)
    }

    @PostConstruct
    fun initBlockChain() {
        initDelegates()
        initStakeholders()

        if (0 == blockService.getAll().size) {
            blockService.add(createGenesisBlock())
        }

        log.info("Block chain successfully initialized")
    }

    @Scheduled(fixedDelayString = "2000")
    fun createTransactionSchedule() {
        val dto = createRandomTransaction()
        val trx = voteTransactionService.add(dto)
        log.info("Created new transaction {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(trx))
    }

    @Scheduled(fixedDelayString = "15000")
    fun createBlockSchedule() {
        val transactions =  mutableSetOf<BaseTransactionDto>()
        transactions.addAll(transferTransactionService.getAllPending().map { TransferTransactionDto(it) })
        transactions.addAll(voteTransactionService.getAllPending().map { VoteTransactionDto(it) })

        if (CollectionUtils.isEmpty(transactions)) {
            return
        }

        val block = blockService.add(createBlock(transactions))
        log.info("Created new block {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(block))
    }

    // todo temp solution
    private fun createGenesisBlock(): MainBlockDto {
        val previousHash = HashUtils.generateHash("previousHash".toByteArray())
        val merkleRoot = HashUtils.generateHash("merkleHash".toByteArray())
        val hash = HashUtils.bytesToHexString(BlockUtils.calculateHash(previousHash, merkleRoot, 0, 0))

        return MainBlockDto(hash, 0, previousHash, merkleRoot, 0,
            "genesis_signature", mutableSetOf())
    }

    @Deprecated("generate random transaction")
    private fun createRandomTransaction(): VoteTransactionDto {
        val amount = Random().nextLong()
        val delegates = delegateService.getActiveDelegates()
        val stakeholders = stakeholderService.getAll()

        val recipientKey = stakeholders[Random().nextInt(2)].publicKey
        val delegateKey = delegates[Random().nextInt(21)].networkId
        val type = if (amount % 2 == 0L) VoteType.FOR else VoteType.AGAINST

        val data = VoteTransactionData(amount, recipientKey, recipientKey, "senderSignature",
            type, delegateKey, 1)
        return voteTransactionService.create(data)
    }

    @Deprecated("generate block")
    private fun createBlock(transactions: MutableSet<out BaseTransactionDto>): MainBlockDto {
        return blockService.create(transactions)
    }

    private fun initDelegates() {
        var port = 9090
        val host = "localhost"
        for (i in 1..21) {
            delegateService.save(Delegate(host + port, host, port))
            port += i
        }
    }

    private fun initStakeholders() {
        for (i in 1..21) {
            val key = UUID.randomUUID().toString()
            stakeholderService.save(Stakeholder(key, key.substring(0, 10)))
        }
    }

}