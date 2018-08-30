package io.openfuture.chain.rpc.controller


import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.rpc.domain.DelegateResponse
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.base.PageResponse
import org.springframework.data.domain.PageImpl
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.stream.Collectors

@CrossOrigin
@RestController
@RequestMapping("/rpc/delegates")
class DelegateController(
    private val delegateService: DelegateService,
    private val genesisBlockService: GenesisBlockService
) {

    @GetMapping
    fun getAll(request: PageRequest): PageResponse<DelegateResponse> =
        PageResponse(delegateService.getAll(request).map { DelegateResponse(it) })

    @GetMapping("/active")
    fun getAllActive(request: PageRequest): PageResponse<DelegateResponse> {
        val activeDelegates = genesisBlockService.getLast().payload.activeDelegates.map { DelegateResponse(it) }
        val pageActiveDelegate = activeDelegates.stream()
            .skip(request.offset)
            .limit(request.getLimit().toLong())
            .collect(Collectors.toList())

        return PageResponse(PageImpl(pageActiveDelegate, request, activeDelegates.size.toLong()))
    }

}