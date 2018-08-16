package io.openfuture.chain.rpc.controller.block

import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.base.PageResponse
import io.openfuture.chain.rpc.domain.block.MainBlockResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/rpc/blocks/main")
class MainBlockController(
    private val epochService: EpochService,
    private val blockService: MainBlockService
) {

    @GetMapping("/{hash}")
    fun get(@PathVariable hash: String): MainBlockResponse = getMainBlockResponse(blockService.getByHash(hash))

    @GetMapping("/previousHash/{previousHash}")
    fun getByPreviousHash(@PathVariable previousHash: String): MainBlockResponse =
        getMainBlockResponse(blockService.getByPreviousHash(previousHash))

    @GetMapping
    fun getAll(request: PageRequest): PageResponse<MainBlock> {
        return PageResponse(blockService.getAll(request))
    }

    private fun getMainBlockResponse(block: MainBlock): MainBlockResponse = MainBlockResponse(block, epochService.getEpochByBlock(block))

}
