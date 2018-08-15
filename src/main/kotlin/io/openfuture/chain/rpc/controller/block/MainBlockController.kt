package io.openfuture.chain.rpc.controller.block

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.base.PageResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/rpc/blocks/main")
class MainBlockController(
    private val blockService: MainBlockService
) {

    @GetMapping
    fun getAll(request: PageRequest): PageResponse<MainBlock> {
        return PageResponse(blockService.getAll(request))
    }

}
