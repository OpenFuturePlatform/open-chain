package io.openfuture.chain.rpc.controller


import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.base.PageResponse
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
@RequestMapping("/rpc/delegates")
class DelegateController(
    private val delegateService: DelegateService
) {

    @GetMapping
    fun getAll(request: PageRequest): PageResponse<Delegate> = PageResponse(delegateService.getAll(request))

}