package io.openfuture.chain.component

import io.openfuture.chain.domain.block.BlockRequest
import io.openfuture.chain.domain.transaction.TransactionRequest
import io.openfuture.chain.nio.client.handler.ConnectionClientHandler
import io.openfuture.chain.service.BlockService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.CollectionUtils
import java.util.*
import javax.annotation.PostConstruct

@Component
class BlockChain(
        private val blockService: BlockService
) {

    companion object {
        const val GENESIS_DIFFICULTY = 2
        const val GENESIS_ORDER_NUMBER = 1
        const val GENESIS_TIMESTAMP = 0L
        const val GENESIS_PREVIOUS_HASH = "empty_hash"
        private val log = LoggerFactory.getLogger(ConnectionClientHandler::class.java)
    }

    var pendingTransactions: MutableList<TransactionRequest> = mutableListOf()

    @PostConstruct
    fun initBlockChain() {
        val blocks = blockService.getAll()

        if (!CollectionUtils.isEmpty(blocks)) {
            return
        }
        initGenesisBlock()
    }

    fun addBlock(blockRequest: BlockRequest) {
        if (blockRequest.isValid()) {
            blockService.save(blockRequest)
        }
    }

    fun addTransaction(transactionRequest: TransactionRequest) {
        this.pendingTransactions.add(transactionRequest)
    }

    fun minePendingTransactions() {
        val lastBlock = blockService.getLast()
        val blockRequest = BlockRequest(getDifficulty(), getCurrentTime(), lastBlock.orderNumber++,
                lastBlock.previousHash, getPrivateKey(), getPublicKey(), pendingTransactions.toList())
        blockService.save(blockRequest)
//        sendBlockCreatedNotification(blockRequest)
    }

    private fun initGenesisBlock() {
        val privateKey = getPrivateKey()
        val publicKey = getPublicKey()
        val blockRequest = BlockRequest(GENESIS_DIFFICULTY, GENESIS_TIMESTAMP, GENESIS_ORDER_NUMBER,
                GENESIS_PREVIOUS_HASH, privateKey, publicKey, listOf())
        blockService.save(blockRequest)
    }

    // todo temp solution; waiting key logic
    private fun getPrivateKey(): String {
        return "node_private_key"
    }

    // todo temp solution; waiting key logic
    private fun getPublicKey(): String {
        return "node_public_key"
    }

    // todo temp solution; waiting timesinc logic
    private fun getCurrentTime(): Long {
        return Date().time
    }

    // todo temp solution; waiting info about consensus
    private fun getDifficulty(): Int {
        return GENESIS_DIFFICULTY
    }

}