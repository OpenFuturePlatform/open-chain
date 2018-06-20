package io.openfuture.chain.component

import io.openfuture.chain.domain.block.BlockRequest
import io.openfuture.chain.domain.transaction.TransactionRequest
import io.openfuture.chain.nio.client.handler.ConnectionClientHandler
import io.openfuture.chain.service.BlockService
import io.openfuture.chain.util.GenesisUtils
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
        if (pendingTransactions.size > 3) { //todo test consensus
            minePendingTransactions()
        }
    }

    fun minePendingTransactions() {
        val lastBlock = blockService.getLast()
        val nextOrderNumber = lastBlock.orderNumber + 1
        val transactions = pendingTransactions.toList()
        val blockRequest = BlockRequest(getDifficulty(), getCurrentTime(), nextOrderNumber, lastBlock.hash,
                getPrivateKey(), getPublicKey(), transactions)
        pendingTransactions.removeAll(transactions)
        blockService.save(blockRequest) //todo need change to send and then processing request as new block
    }

    private fun initGenesisBlock() {
        blockService.save(GenesisUtils.genesisBlock)
    }

    // todo temp solution; waiting key logic
    private fun getPrivateKey(): String {
        return "node_private_key"
    }

    // todo temp solution; waiting key logic
    private fun getPublicKey(): String {
        return "node_public_key"
    }

    // todo temp solution; waiting timesync logic
    private fun getCurrentTime(): Long {
        return Date().time
    }

    // todo temp solution; waiting info about consensus
    private fun getDifficulty(): Int {
        return 2
    }

}