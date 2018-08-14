package io.openfuture.chain.rpc.controller

import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.TransactionService
import io.openfuture.chain.network.service.NetworkApiService
import io.openfuture.chain.rpc.domain.explorer.ExplorerResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/rpc/explorer/info")
class ExplorerController(
    private val transactionService: TransactionService,
    private val blockService: BlockService,
    private val epochService: EpochService,
    private val networkApiService: NetworkApiService
) {

    @GetMapping
    fun getExplorerInfo(): ExplorerResponse {
        val blocksCount = blockService.getCount()
        val blocksSpeed = 1.0
        val transactionsCount = transactionService.getCount()
        val transactionsSpeed = 1.0
        val nodesCount = networkApiService.getNetworkSize()
        val epochNumber = epochService.getEpochIndex()
        val epochDate = epochService.getEpochStart()
        val delegatesCount = epochService.getDelegates().size

        return ExplorerResponse(nodesCount, blocksCount, transactionsCount, blocksSpeed, transactionsSpeed, epochNumber,
            epochDate, delegatesCount.toByte())
    }

}

