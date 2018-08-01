package io.openfuture.chain.rpc.controller

import io.openfuture.chain.consensus.model.entity.Delegate
import io.openfuture.chain.consensus.service.DelegateService
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.base.PageResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/rpc/delegates")
class DelegateController(
    private val delegateService: DelegateService) {

    @GetMapping
    fun getAll(request: PageRequest): PageResponse<Delegate> {
        return PageResponse(delegateService.getAll(request))
    }

}