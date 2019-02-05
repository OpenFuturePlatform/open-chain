package io.openfuture.chain.rpc.controller.block

import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.service.MainBlockService
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
    private val blockService: MainBlockService
) {

    @GetMapping("/{hash}")
    fun get(@PathVariable hash: String): MainBlockResponse = getMainBlockResponse(blockService.getByHash(hash))

    @GetMapping("/{hash}/previous")
    fun getPreviousBlock(@PathVariable hash: String): MainBlockResponse? =
        getMainBlockResponse(blockService.getPreviousBlock(hash))

    @GetMapping("/{hash}/next")
    fun getNextBlock(@PathVariable hash: String): MainBlockResponse? =
        getMainBlockResponse(blockService.getNextBlock(hash))

    @GetMapping
    fun getAll(@Valid request: PageRequest): PageResponse<MainBlockResponse> =
        PageResponse(blockService.getAll(request).map { getMainBlockResponse(it) })

    private fun getMainBlockResponse(block: MainBlock): MainBlockResponse = MainBlockResponse(block, epochService.getEpochByBlock(block))

}
