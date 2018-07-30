package io.openfuture.chain.rpc.controller

import io.openfuture.chain.domain.base.PageRequest
import io.openfuture.chain.domain.base.PageResponse
import io.openfuture.chain.entity.Delegate
import io.openfuture.chain.rpc.controller.base.BaseController
import io.openfuture.chain.rpc.domain.RestResponse
import io.openfuture.chain.service.DelegateService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/rpc/delegates")
class DelegateController(
    private val delegateService: DelegateService
): BaseController() {

    @GetMapping
    fun getAll(request: PageRequest): RestResponse<PageResponse<Delegate>> {
        val body = PageResponse(delegateService.getAll(request))
        return RestResponse(getResponseHeader(), body)
    }

}