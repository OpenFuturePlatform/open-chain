package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.service.TransactionManager
import io.openfuture.chain.rpc.domain.transaction.request.DelegateTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.response.DelegateTransactionResponse
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/rpc/transactions/delegate")
class DelegateTransactionController(
    private val transactionManager: TransactionManager
) {

    @CrossOrigin
    @GetMapping("/{hash}")
    fun get(@PathVariable hash: String): DelegateTransactionResponse =
        DelegateTransactionResponse(transactionManager.getDelegateTransactionByHash(hash))

    @PostMapping
    fun add(@Valid @RequestBody request: DelegateTransactionRequest): DelegateTransactionResponse {
        val tx = transactionManager.add(UnconfirmedDelegateTransaction.of(request))
        return DelegateTransactionResponse(tx)
    }

}