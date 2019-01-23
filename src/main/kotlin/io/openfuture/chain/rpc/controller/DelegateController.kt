package io.openfuture.chain.rpc.controller


import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.DelegateStateService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.WalletStateService
import io.openfuture.chain.core.service.state.DefaultDelegateStateService.Companion.DEFAULT_DELEGATE_RATING
import io.openfuture.chain.rpc.domain.DelegateResponse
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.base.PageResponse
import io.openfuture.chain.rpc.domain.view.ViewDelegatePageRequest
import io.openfuture.chain.rpc.domain.view.ViewDelegateResponse
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
    private val delegateService: DelegateService,
    private val delegateStateService: DelegateStateService,
    private val walletStateService: WalletStateService,
    private val genesisBlockService: GenesisBlockService
) {

    @GetMapping
    fun getAll(request: PageRequest): PageResponse<DelegateResponse> {
        val delegates = delegateStateService.getAllDelegates(request).map {
            DelegateResponse(delegateService.getByPublicKey(it.address))
        }

        return PageResponse(PageImpl(delegates, request, delegates.size.toLong()))
    }

    @GetMapping("/active")
    fun getAllActive(request: PageRequest): PageResponse<DelegateResponse> {
        val activeDelegates = genesisBlockService.getLast().payload.activeDelegates.map {
            DelegateResponse(delegateService.getByPublicKey(it))
        }

        val pageActiveDelegate = activeDelegates.drop(request.offset.toInt()).take(request.getLimit())
        return PageResponse(PageImpl(pageActiveDelegate, request, activeDelegates.size.toLong()))
    }

    @GetMapping("/view")
    fun getAll(@Valid request: ViewDelegatePageRequest): PageResponse<ViewDelegateResponse> {
        val delegates = delegateStateService.getAllDelegates(request).map {
            val delegate = delegateService.getByPublicKey(it.address)
            val state = delegateStateService.getLastByAddress(it.address)
            ViewDelegateResponse(
                delegate.address,
                delegate.publicKey,
                state?.rating ?: DEFAULT_DELEGATE_RATING,
                walletStateService.getVotesForDelegate(delegate.publicKey).size,
                delegate.registrationDate
            )
        }.sortedByDescending { it.rating }

        return PageResponse(PageImpl(delegates, request, delegates.size.toLong()))
    }

}