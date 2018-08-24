package io.openfuture.chain.rpc.controller


import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.rpc.domain.DelegateResponse
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.base.PageResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/rpc/delegates")
class DelegateController(
    private val delegateService: DelegateService,
    private val genesisBlockService: GenesisBlockService
) {

    @GetMapping("/active")
    fun getAllActive(): List<DelegateResponse> = genesisBlockService.getLast().payload.activeDelegates.map { DelegateResponse(it) }

    @GetMapping
    fun getAll(request: PageRequest): PageResponse<DelegateResponse> =
        PageResponse(delegateService.getAll(request).map { DelegateResponse(it) })

}