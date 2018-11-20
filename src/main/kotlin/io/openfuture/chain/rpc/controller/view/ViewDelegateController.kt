package io.openfuture.chain.rpc.controller.view

import io.openfuture.chain.core.service.ViewDelegateService
import io.openfuture.chain.rpc.domain.base.PageResponse
import io.openfuture.chain.rpc.domain.view.ViewDelegatePageRequest
import io.openfuture.chain.rpc.domain.view.ViewDelegateResponse
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@CrossOrigin
@RestController
@RequestMapping("/rpc/delegates/view")
class ViewDelegateController(
    private val viewDelegateService: ViewDelegateService
) {

    @GetMapping
    fun getAll(@Valid request: ViewDelegatePageRequest): PageResponse<ViewDelegateResponse> =
        PageResponse(viewDelegateService.getAll(request).map { ViewDelegateResponse(it) })

}