package io.openfuture.chain.rpc.controller.transaction

import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.rpc.domain.transaction.request.delegate.DelegateTransactionHashRequest
import io.openfuture.chain.rpc.domain.transaction.request.delegate.DelegateTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.response.DelegateTransactionResponse
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/rpc/transactions/delegates")
class DelegateTransactionController(
    private val transactionService: DelegateTransactionService,
    private val blockService: GenesisBlockService
) {

    @PostMapping("/doGenerateHash")
    fun getHash(@Valid @RequestBody request: DelegateTransactionHashRequest): String {
        return transactionService.generateHash(request)
    }

    @PostMapping
    fun add(@Valid @RequestBody request: DelegateTransactionRequest): DelegateTransactionResponse {
        val tx = transactionService.add(request)
        return DelegateTransactionResponse(tx)
    }

    @GetMapping
    fun create() {
        val create = blockService.create()
        blockService.add(create)
    }

}

