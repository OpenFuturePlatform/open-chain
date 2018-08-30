package io.openfuture.chain.rpc.controller

import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.TransactionService
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
    private val blockService: BlockService,
    private val epochService: EpochService,
    private val networkApiService: NetworkApiService,
    private val transactionService: TransactionService
) {

    @GetMapping
    fun getExplorerInfo(): ExplorerResponse {
        val blocksCount = blockService.getCount()
        val secondsPerBlock = blockService.getAvgProductionTime()
        val transactionsCount = transactionService.getCount()
        val transactionsPerSecond = transactionService.getProducingPerSecond()
        val nodesCount = networkApiService.getNetworkSize()
        val epochNumber = epochService.getEpochIndex()
        val epochDate = epochService.getEpochStart()
        val delegatesCount = epochService.getDelegates().size

        return ExplorerResponse(nodesCount, blocksCount, transactionsCount, secondsPerBlock, transactionsPerSecond, epochNumber,
            epochDate, delegatesCount.toByte())
    }

}

