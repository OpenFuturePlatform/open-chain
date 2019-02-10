package io.openfuture.chain.rpc.controller

import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.service.BaseTransactionService
import io.openfuture.chain.core.service.BlockService
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
    private val baseTransactionService: BaseTransactionService
) {

    @GetMapping
    fun getExplorerInfo(): ExplorerResponse {
        val blocksCount = blockService.getCount()
        val blockProductionTime = blockService.getAvgProductionTime()
        val transactionsCount = baseTransactionService.getCount()
        val transactionsPerSecond = baseTransactionService.getProducingPerSecond()
        val nodesCount = networkApiService.getNetworkSize()
        val epochNumber = epochService.getEpochIndex()
        val epochDate = epochService.getEpochStart()
        val delegatesCount = epochService.getDelegatesPublicKeys().size

        return ExplorerResponse(nodesCount, blocksCount, transactionsCount, blockProductionTime, transactionsPerSecond, epochNumber,
            epochDate, delegatesCount.toByte())
    }

}

