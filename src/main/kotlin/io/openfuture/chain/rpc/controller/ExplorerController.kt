package io.openfuture.chain.rpc.controller

import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.service.BlockManager
import io.openfuture.chain.core.service.TransactionManager
import io.openfuture.chain.network.service.NetworkApiService
import io.openfuture.chain.rpc.domain.explorer.ExplorerResponse
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
@RequestMapping("/rpc/explorer/info")
class ExplorerController(
    private val blockManager: BlockManager,
    private val epochService: EpochService,
    private val networkApiService: NetworkApiService,
    private val transactionManager: TransactionManager
) {

    @GetMapping
    fun getExplorerInfo(): ExplorerResponse {
        val blocksCount = blockManager.getCount()
        val blockProductionTime = blockManager.getAvgProductionTime()
        val transactionsCount = transactionManager.getCount()
        val transactionsPerSecond = transactionManager.getProducingPerSecond()
        val nodesCount = networkApiService.getNetworkSize()
        val epochNumber = epochService.getEpochIndex()
        val epochDate = epochService.getEpochStart()
        val delegatesCount = epochService.getDelegatesPublicKeys().size

        return ExplorerResponse(nodesCount, blocksCount, transactionsCount, blockProductionTime, transactionsPerSecond, epochNumber,
            epochDate, delegatesCount.toByte())
    }

}

