package io.openfuture.chain.rpc.controller

import io.openfuture.chain.core.service.BlockManager
import io.openfuture.chain.core.service.StateManager
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.base.PageResponse
import io.openfuture.chain.rpc.domain.delegate.DelegateResponse
import io.openfuture.chain.rpc.domain.delegate.ViewDelegatePageRequest
import io.openfuture.chain.rpc.domain.delegate.ViewDelegateResponse
import org.springframework.data.domain.PageImpl
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@CrossOrigin
@RestController
@RequestMapping("/rpc/delegates")
class DelegateController(
    private val stateManager: StateManager,
    private val blockManager: BlockManager
) {

    @GetMapping
    fun getAll(@Valid request: PageRequest): PageResponse<DelegateResponse> {
        val delegates = stateManager.getAllDelegates(request).map { DelegateResponse(it) }
        return PageResponse(delegates)
    }

    @GetMapping("/active")
    fun getAllActive(@Valid request: PageRequest): PageResponse<DelegateResponse> {
        val activeDelegates = blockManager.getLastGenesisBlock().getPayload().activeDelegates.map {
            DelegateResponse(stateManager.getByAddress(it))
        }

        val pageActiveDelegate = activeDelegates.drop(request.offset.toInt()).take(request.getLimit())
        return PageResponse(PageImpl(pageActiveDelegate, request, activeDelegates.size.toLong()))
    }

    @GetMapping("/view")
    fun getAll(@Valid request: ViewDelegatePageRequest): PageResponse<ViewDelegateResponse> {
        val delegates = stateManager.getAllDelegates(request)
        val pageDelegates = delegates.map { delegate ->
            ViewDelegateResponse(delegate, stateManager.getVotesForDelegate(delegate.address).size)
        }.sortedByDescending { it.rating }

        return PageResponse(PageImpl(pageDelegates, request, delegates.totalElements))
    }

}