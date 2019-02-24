package io.openfuture.chain.rpc.controller.block

import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.service.BlockManager
import io.openfuture.chain.core.service.TransactionManager
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.base.PageResponse
import io.openfuture.chain.rpc.domain.block.MainBlockResponse
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@CrossOrigin
@RestController
@RequestMapping("/rpc/blocks/main")
class MainBlockController(
    private val epochService: EpochService,
    private val blockManager: BlockManager,
    private val transactionManager: TransactionManager
) {

    @GetMapping("/{hash}")
    fun get(@PathVariable hash: String): MainBlockResponse = getMainBlockResponse(blockManager.getMainBlockByHash(hash))

    @GetMapping("/{hash}/previous")
    fun getPreviousBlock(@PathVariable hash: String): MainBlockResponse? =
        getMainBlockResponse(blockManager.getPreviousMainBlock(hash))

    @GetMapping("/{hash}/next")
    fun getNextBlock(@PathVariable hash: String): MainBlockResponse? =
        getMainBlockResponse(blockManager.getNextMainBlock(hash))

    @GetMapping
    fun getAll(@Valid request: PageRequest): PageResponse<MainBlockResponse> =
        PageResponse(blockManager.getAllMainBlocks(request).map { getMainBlockResponse(it) })

    private fun getMainBlockResponse(block: MainBlock): MainBlockResponse =
        MainBlockResponse(block, transactionManager.getCountByBlock(block),
            if (block.height == 0L) 0 else epochService.getEpochByBlock(block))

}
