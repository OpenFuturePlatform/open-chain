package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.rpc.domain.transaction.request.DelegateTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.response.DelegateTransactionResponse
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/rpc/transactions/delegate")
class DelegateTransactionController(
    private val transactionService: DelegateTransactionService
) {

    @GetMapping("/{hash}")
    fun get(@PathVariable hash: String): DelegateTransaction = transactionService.getByHash(hash)

    @PostMapping
    fun add(@Valid @RequestBody request: DelegateTransactionRequest): DelegateTransactionResponse {
        val tx = transactionService.add(request)
        return DelegateTransactionResponse(tx)
    }

}

