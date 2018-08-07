package io.openfuture.chain.rpc.controller.block

import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.base.PageResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/rpc/blocks/genesis")
class GenesisBlockController(
    private val blockService: GenesisBlockService) {

    @GetMapping
    fun getAll(request: PageRequest): PageResponse<GenesisBlock> {
        return PageResponse(blockService.getAll(request))
    }

}