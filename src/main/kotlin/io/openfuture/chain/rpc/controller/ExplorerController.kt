package io.openfuture.chain.rpc.controller

import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.TransactionService
import io.openfuture.chain.rpc.domain.explorer.ExplorerDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/rpc/explorer/info")
class ExplorerController(
    private val transactionService: TransactionService,
    private val blockService: BlockService,
    private val delegateService: DelegateService
) {

    @GetMapping
    fun getExplorerInfo(): ExplorerDto {
        val blocksCount = blockService.getCount()
        val blocksSpeed = 1.0
        val transactionsCount = transactionService.getCount()
        val transactionsSpeed = 1.0
        val nodesCount = 0L//networkService.nodesCount()
        val epochNumber = 0L//blockService.epochNumber()
        val epochDate = Date()//blockService.epochDate()
        val delegatesCount = 0L//delegateService.count()

        return ExplorerDto(nodesCount, blocksCount, transactionsCount, blocksSpeed, transactionsSpeed, epochNumber,
            epochDate, delegatesCount)
    }

}

