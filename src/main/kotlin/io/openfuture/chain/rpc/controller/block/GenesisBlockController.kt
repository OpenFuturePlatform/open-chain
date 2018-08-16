package io.openfuture.chain.rpc.controller.block

import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.base.PageResponse
import io.openfuture.chain.rpc.domain.block.GenesisBlockResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/rpc/blocks/genesis")
class GenesisBlockController(
    private val blockService: GenesisBlockService
) {

    @GetMapping("/{hash}")
    fun get(@PathVariable hash: String): GenesisBlockResponse = GenesisBlockResponse(blockService.getByHash(hash))

    @GetMapping("/previousHash/{previousHash}")
    fun getByPreviousHash(@PathVariable previousHash: String): GenesisBlockResponse =
        GenesisBlockResponse(blockService.getByPreviousHash(previousHash))

    @GetMapping
    fun getAll(request: PageRequest): PageResponse<GenesisBlock> {
        return PageResponse(blockService.getAll(request))
    }

}