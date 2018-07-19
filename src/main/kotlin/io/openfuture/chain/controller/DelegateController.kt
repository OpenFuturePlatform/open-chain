package io.openfuture.chain.controller

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.controller.common.ResponseHeader
import io.openfuture.chain.controller.common.RestResponse
import io.openfuture.chain.domain.base.PageRequest
import io.openfuture.chain.domain.base.PageResponse
import io.openfuture.chain.domain.rpc.node.NodeVersionResponse
import io.openfuture.chain.service.DelegateService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("${PathConstant.RPC}/delegates")
class DelegateController(
    private val delegateService: DelegateService,
    private val nodeClock: NodeClock
) {

    @GetMapping
    fun getAll(request: PageRequest): RestResponse {
        val body = PageResponse(delegateService.getAll(request))
        return RestResponse(ResponseHeader(nodeClock.networkTime(), NodeVersionResponse().version), body)
    }

}