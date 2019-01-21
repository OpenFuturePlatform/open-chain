package io.openfuture.chain.rpc.controller


import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.DelegateStateService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.WalletVoteService
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
import java.util.stream.Collectors
import javax.validation.Valid

@CrossOrigin
@RestController
@RequestMapping("/rpc/delegates")
class DelegateController(
    private val delegateService: DelegateService,
    private val delegateStateService: DelegateStateService,
    private val walletVoteService: WalletVoteService,
    private val genesisBlockService: GenesisBlockService
) {

    @GetMapping
    fun getAll(request: PageRequest): PageResponse<DelegateResponse> =
        PageResponse(delegateService.getAll(request).map { DelegateResponse(it) })

    @GetMapping("/active")
    fun getAllActive(request: PageRequest): PageResponse<DelegateResponse> {
        val activeDelegates = genesisBlockService.getLast().payload.activeDelegates.map {
            DelegateResponse(delegateService.getByPublicKey(it))
        }

        val pageActiveDelegate = activeDelegates.stream()
            .skip(request.offset)
            .limit(request.getLimit().toLong())
            .collect(Collectors.toList())

        return PageResponse(PageImpl(pageActiveDelegate, request, activeDelegates.size.toLong()))
    }

    @GetMapping("/view")
    fun getAll(@Valid request: ViewDelegatePageRequest): PageResponse<ViewDelegateResponse> {
        val activeDelegates = genesisBlockService.getLast().payload.activeDelegates.map { publicKey ->
            val delegate = delegateService.getByPublicKey(publicKey)
            val state = delegateStateService.getLastByAddress(publicKey)

            ViewDelegateResponse(
                delegate.address,
                delegate.publicKey,
                delegate.publicKey, // todo remove for front
                state?.rating ?: DEFAULT_DELEGATE_RATING,
                walletVoteService.getVotesForDelegate(delegate.publicKey).size,
                delegate.registrationDate
            )
        }

        val pageActiveDelegate = activeDelegates.stream()
            .skip(request.offset)
            .limit(request.getLimit().toLong())
            .collect(Collectors.toList())

        return PageResponse(PageImpl(pageActiveDelegate, request, activeDelegates.size.toLong()))
    }

}