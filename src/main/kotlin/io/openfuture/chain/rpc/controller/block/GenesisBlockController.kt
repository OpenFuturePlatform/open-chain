package io.openfuture.chain.rpc.controller.block

import io.openfuture.chain.core.service.BlockManager
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.base.PageResponse
import io.openfuture.chain.rpc.domain.block.GenesisBlockResponse
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@CrossOrigin
@RestController
@RequestMapping("/rpc/blocks/genesis")
class GenesisBlockController(
    private val blockManager: BlockManager
) {

    @GetMapping("/{hash}")
    fun get(@PathVariable hash: String): GenesisBlockResponse =
        GenesisBlockResponse(blockManager.getGenesisBlockByHash(hash))

    @GetMapping("/{hash}/previous")
    fun getPreviousBlock(@PathVariable hash: String): GenesisBlockResponse? =
        GenesisBlockResponse(blockManager.getPreviousGenesisBlock(hash))

    @GetMapping("/{hash}/next")
    fun getNextBlock(@PathVariable hash: String): GenesisBlockResponse? =
        GenesisBlockResponse(blockManager.getNextGenesisBlock(hash))

    @GetMapping
    fun getAll(@Valid request: PageRequest): PageResponse<GenesisBlockResponse> =
        PageResponse(blockManager.getAllGenesisBlocks(request).map { GenesisBlockResponse(it) })

}