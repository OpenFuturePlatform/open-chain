package io.openfuture.chain.component

import io.openfuture.chain.domain.block.BlockRequest
import io.openfuture.chain.domain.block.GenesisBlock
import io.openfuture.chain.domain.transaction.TransactionRequest
import io.openfuture.chain.nio.client.handler.ConnectionClientHandler
import io.openfuture.chain.service.BlockService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
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
        if (isEmptyBlockChain()) {
            initGenesisBlock()
        }
//        updateBlockChainFormNetwork() //todo update blockchain from master nodes
    }

    private fun isEmptyBlockChain(): Boolean {
        return 0L == blockService.count()
    }

    @Transactional
    fun addBlock(blockRequest: BlockRequest) {
        val lastBlock = blockService.getLast()

        if (lastBlock.hash != blockRequest.previousHash) {
            return //todo invalid block
        }
                
        if (!blockRequest.isValid()) {
            return // todo invalid block
        }
        blockService.save(blockRequest)
        pendingTransactions.removeAll(blockRequest.transactions)
    }

    fun addPendingTransaction(transactionRequest: TransactionRequest) {
        this.pendingTransactions.add(transactionRequest)
    }

    fun minePendingTransactions() {
        val lastBlock = blockService.getLast()
        val nextOrderNumber = lastBlock.orderNumber + 1
        val transactions = pendingTransactions.toList()
        val blockRequest = BlockRequest(getDifficulty(), getCurrentTime(), nextOrderNumber, lastBlock.hash,
                getPrivateKey(), getPublicKey(), transactions)
//        sendCreateBlockNotification(blockRequest) //todo send new block
    }

    private fun initGenesisBlock() {
        blockService.save(GenesisBlock.instance)
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