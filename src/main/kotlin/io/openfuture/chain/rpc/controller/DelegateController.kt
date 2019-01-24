package io.openfuture.chain.rpc.controller


import io.openfuture.chain.core.service.DelegateStateService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.WalletStateService
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
    private val delegateStateService: DelegateStateService,
    private val walletStateService: WalletStateService,
    private val genesisBlockService: GenesisBlockService
) {

    @GetMapping
    fun getAll(request: PageRequest): PageResponse<DelegateResponse> {
        val delegates = delegateStateService.getAllDelegates(request).map { DelegateResponse(it) }
        return PageResponse(PageImpl(delegates, request, delegates.size.toLong()))
    }

    @GetMapping("/active")
    fun getAllActive(request: PageRequest): PageResponse<DelegateResponse> {
        val activeDelegates = genesisBlockService.getLast().payload.activeDelegates.map {
            DelegateResponse(delegateStateService.getLastByAddress(it)!!)
        }

        val pageActiveDelegate = activeDelegates.drop(request.offset.toInt()).take(request.getLimit())
        return PageResponse(PageImpl(pageActiveDelegate, request, activeDelegates.size.toLong()))
    }

    @GetMapping("/delegate")
    fun getAll(@Valid request: ViewDelegatePageRequest): PageResponse<ViewDelegateResponse> {
        val delegates = delegateStateService.getAllDelegates(request).map { delegate ->
            ViewDelegateResponse(delegate, walletStateService.getVotesForDelegate(delegate.address).size)
        }.sortedByDescending { it.rating }

        return PageResponse(PageImpl(delegates, request, delegates.size.toLong()))
    }

}